package com.wishtoday.ts.simpleminer.undo.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

//这是AI做的哈。。。---by deepseek-v4.1-flash-1017
/**
 * 撤回界面的统一配色与绘制工具,让列表页和详情页看起来是一套东西。
 */
@Environment(EnvType.CLIENT)
public final class UndoTheme {
    /** 面板底色(接近不透明的深蓝黑) */
    public static final int PANEL_BG = 0xF0121420;
    /** 面板描边 */
    public static final int PANEL_BORDER = 0xFF3A4360;
    /** 标题栏底色 */
    public static final int TITLE_BG = 0xFF1C2233;
    /** 强调色(标题下的高亮线/选中项) */
    public static final int ACCENT = 0xFF4FC3F7;
    /** 主要文字 */
    public static final int TEXT = 0xFFECEFF4;
    /** 次要文字(时间、提示) */
    public static final int TEXT_DIM = 0xFF9AA6BF;
    /** 隔行底色 */
    public static final int ROW_ALT = 0x10FFFFFF;
    /** 悬停底色 */
    public static final int ROW_HOVER = 0x2EFFFFFF;
    /** 危险色(删除) */
    public static final int DANGER = 0xFFB8434B;
    public static final int DANGER_HOVER = 0xFFFF5C5C;
    /** 完成色(材料已取够) */
    public static final int SUCCESS = 0xFF7BD88F;
    /** 未完成色 */
    public static final int WARNING = 0xFFE8B24C;
    /** 槽位边框 */
    public static final int SLOT_FRAME = 0xFF2A3145;
    public static final int SLOT_FRAME_LIGHT = 0xFF4A5470;

    private UndoTheme() {
    }

    /** 深色底 + 1px 描边 */
    public static void panel(DrawContext context, int x, int y, int width, int height) {
        context.fill(x, y, x + width, y + height, PANEL_BG);
        context.fill(x, y, x + width, y + 1, PANEL_BORDER);
        context.fill(x, y + height - 1, x + width, y + height, PANEL_BORDER);
        context.fill(x, y, x + 1, y + height, PANEL_BORDER);
        context.fill(x + width - 1, y, x + width, y + height, PANEL_BORDER);
    }

    /** 标题栏:深色底 + 底部渐隐强调线 */
    public static void titleBar(DrawContext context, int x, int y, int width, int height, Text title) {
        context.fill(x + 1, y + 1, x + width - 1, y + height, TITLE_BG);
        context.fillGradient(x + 1, y + height - 1, x + width - 1, y + height, ACCENT, 0x004FC3F7);
        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        context.drawText(tr, title, x + 8, y + (height - tr.fontHeight) / 2 + 1, TEXT, false);
    }

    /** 右上角关闭按钮 */
    public static void closeButton(DrawContext context, int x, int y, int size, boolean hovered) {
        if (hovered) {
            context.fill(x, y, x + size, y + size, 0x55FF5555);
        }
        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        int color = hovered ? 0xFFFF9999 : TEXT_DIM;
        int tw = tr.getWidth("x");
        context.drawText(tr, "x", x + (size - tw) / 2, y + (size - tr.fontHeight) / 2 + 1, color, false);
    }

    /** 物品槽位外框(用于提交槽) */
    public static void slotFrame(DrawContext context, int x, int y) {
        context.fill(x - 2, y - 2, x + 18, y + 18, SLOT_FRAME);
        context.fill(x - 1, y - 1, x + 17, y + 17, SLOT_FRAME_LIGHT);
        context.fill(x, y, x + 16, y + 16, PANEL_BG);
    }

    /** 横向居中绘制文字 */
    public static void centeredText(DrawContext context, Text text, int centerX, int y, int color) {
        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        context.drawText(tr, text, centerX - tr.getWidth(text) / 2, y, color, false);
    }

    /** 细分隔线 */
    public static void divider(DrawContext context, int x, int y, int width) {
        context.fill(x, y, x + width, y + 1, PANEL_BORDER);
    }
}
