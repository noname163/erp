package com.dat.erp.systemconfigs;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.dat.erp.constants.CommonEnum;
import com.dat.erp.entities.SystemApi;
import com.dat.erp.repositories.customrepositories.SystemApiRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Component
@RequiredArgsConstructor
@Log4j2
public class ApiScanner implements ApplicationListener<ContextRefreshedEvent> {

    private final RequestMappingHandlerMapping handlerMapping;
    private final SystemApiRepository systemApiRepository;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = handlerMapping.getHandlerMethods();

        // Use a set to avoid duplicates in memory
        Set<String> discoveredUrls = new HashSet<>();

        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
            RequestMappingInfo mappingInfo = entry.getKey();

            Set<String> urls = new HashSet<>();

            if (mappingInfo.getPathPatternsCondition() != null) {
                mappingInfo.getPathPatternsCondition().getPatterns()
                        .forEach(pathPattern -> urls.add(pathPattern.getPatternString()));
            }

            for (String url : urls) {
                String normalizedUrl = normalizeUrl(url);
                discoveredUrls.add(normalizedUrl);
            }
        }

        if (!discoveredUrls.isEmpty()) {
            // Query DB for existing endpoints

            // Only keep new ones
            List<SystemApi> newApis = discoveredUrls.stream()
                    .map(url -> {
                        SystemApi api = new SystemApi();
                        api.setEndpoint(url);
                        api.setCode(url.replace("/", "_").toUpperCase());
                        api.setSystemType(CommonEnum.BACKEND);
                        api.setDescription("Auto-discovered");
                        return api;
                    })
                    .toList();

            if (!newApis.isEmpty()) {
                try {
                    List<SystemApi> insertedApi = systemApiRepository.saveAll(newApis);
                    for (SystemApi systemApi : insertedApi) {
                        log.info("Discovered new API: {}", systemApi.getEndpoint());
                    }
                } catch (Exception e) {
                    // TODO: handle exception
                }
            } else {
                log.info("No new APIs discovered.");
            }
        }
    }

    private String normalizeUrl(String url) {
        if (url == null || url.isEmpty()) {
            return url;
        }

        if (!url.startsWith("/")) {
            url = "/" + url;
        }

        String[] parts = url.split("/");
        if (parts.length >= 3) {
            return "/" + parts[1] + "/" + parts[2]; // /xxx/xxxx
        } else if (parts.length >= 2) {
            return "/" + parts[1]; // /xxx
        } else {
            return url;
        }
    }

}
