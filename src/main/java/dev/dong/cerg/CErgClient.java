package dev.dong.cerg;

import dev.dong.cerg.content.ClipboardSelectionHandler;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CErgClient {

    public static final ClipboardSelectionHandler CLIPBOARD_HANDLER = new ClipboardSelectionHandler();
}
