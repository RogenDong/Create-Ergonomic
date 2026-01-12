package dev.dong.cerg;

import dev.dong.cerg.content.ClipboardSelectionHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CErgClient {

    public static final ClipboardSelectionHandler CLIPBOARD_HANDLER = new ClipboardSelectionHandler();
}
