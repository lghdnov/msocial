package lghdnov.msocial.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.storage.local.avatar-path:./uploads/avatars}")
    private String avatarPath;

    @Value("${app.storage.local.track-path:./uploads/tracks}")
    private String trackPath;

    @Value("${app.storage.local.post-media-path:./uploads/post-media}")
    private String postMediaPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/avatars/**")
            .addResourceLocations("file:" + avatarPath + "/");

        registry.addResourceHandler("/tracks/**")
            .addResourceLocations("file:" + trackPath + "/");

        registry.addResourceHandler("/post-media/**")
            .addResourceLocations("file:" + postMediaPath + "/");
    }
}
