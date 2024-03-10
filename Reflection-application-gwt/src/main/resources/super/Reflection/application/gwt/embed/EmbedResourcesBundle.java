// File managed by WebFX (DO NOT EDIT MANUALLY)
package Reflection.application.gwt.embed;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;
import dev.webfx.platform.resource.spi.impl.gwt.GwtResourceBundleBase;

public interface EmbedResourcesBundle extends ClientBundle {

    EmbedResourcesBundle R = GWT.create(EmbedResourcesBundle.class);
    @Source("dev/webfx/platform/meta/exe/exe.properties")
    TextResource r1();

    @Source("misc/assets.data")
    TextResource r2();

    @Source("misc/levels.data")
    TextResource r3();

    @Source("misc/solutions.data")
    TextResource r4();



    final class ProvidedGwtResourceBundle extends GwtResourceBundleBase {
        public ProvidedGwtResourceBundle() {
            registerResource("dev/webfx/platform/meta/exe/exe.properties", R.r1());
            registerResource("misc/assets.data", R.r2());
            registerResource("misc/levels.data", R.r3());
            registerResource("misc/solutions.data", R.r4());

        }
    }
}
