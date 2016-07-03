package flaxbeard.steamcraft.gui;

import flaxbeard.steamcraft.Steamcraft;
import flaxbeard.steamcraft.api.SteamcraftRegistry;
import flaxbeard.steamcraft.api.book.BookPage;
import flaxbeard.steamcraft.init.items.tools.GadgetItems;
import flaxbeard.steamcraft.integration.CrossMod;
import flaxbeard.steamcraft.integration.EnchiridionIntegration;
import flaxbeard.steamcraft.item.ItemSteamcraftBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.MutablePair;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiSteamcraftBook extends GuiScreen {
    private static final ResourceLocation bookGuiTextures = new ResourceLocation("steamcraft:textures/gui/book.png");
    private static final ResourceLocation book2 = new ResourceLocation("steamcraft:textures/gui/book2.png");
    private static final ResourceLocation reverseBookGuiTextures = new ResourceLocation("steamcraft:textures/gui/bookReverse.png");
    public static int bookTotalPages = 1;
    public static int currPage = 0;
    public static int lastIndexPage = 0;
    public static String viewing = "";
    private static ItemStack book;
    private static boolean mustReleaseMouse = false;
    public int bookImageWidth = 192;
    public int bookImageHeight = 192;
    private GuiSteamcraftBook.NextPageButton buttonNextPage;
    private GuiSteamcraftBook.NextPageButton buttonPreviousPage;
    private ArrayList<String> categories;

    public GuiSteamcraftBook(EntityPlayer player) {
        categories = new ArrayList<>();
        for (String cat : SteamcraftRegistry.categories) {
            int pages = 0;
            for (MutablePair<String, String> research : SteamcraftRegistry.research) {
                if (research.right.equals(cat) && SteamcraftRegistry.researchPages.get(research.left).length > 0) {
                    pages++;
                }
            }
            for (int s = 0; s < MathHelper.ceiling_float_int(pages / 9.0F); s++) {
                categories.add(cat + (s == 0 ? "" : s));
            }
        }
        bookTotalPages = MathHelper.ceiling_float_int(categories.size() / 2F) + 1;
        if (!viewing.isEmpty()) {
            bookTotalPages = MathHelper.ceiling_float_int(SteamcraftRegistry.researchPages.get(viewing).length / 2F);
        }
        ItemStack active = player.getHeldItemMainhand();
        if (active != null && active.getItem() instanceof ItemSteamcraftBook) {
            book = active;
        } else {
            if (CrossMod.ENCHIRIDION) {
                book = EnchiridionIntegration.findBook(GadgetItems.Items.BOOK.getItem(), player);
            }
            for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
                ItemStack stack = player.inventory.getStackInSlot(i);
                if (stack != null && stack.getItem() instanceof ItemSteamcraftBook) {
                    book = stack;
                    break;
                }
            }
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
    }

    @Override
    public void initGui() {
        buttonList.clear();

        int i = (width - bookImageWidth) / 2;
        int b0 = (height - bookImageHeight) / 2;
        buttonList.add(buttonNextPage = new GuiSteamcraftBook.NextPageButton(1, i + 120 + 67, b0 + 154, true));
        buttonList.add(buttonPreviousPage = new GuiSteamcraftBook.NextPageButton(2, i + 38 - 67, b0 + 154, false));

        updateButtons();
    }

    public void updateButtons() {
        buttonNextPage.visible = (currPage < bookTotalPages - 1);
        buttonPreviousPage.visible = currPage > 0;
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.enabled) {
            if (button.id == 0) {
                mc.displayGuiScreen(null);
            } else if (button.id == 1) {
                if (currPage < bookTotalPages - 1) {
                    ++currPage;
                }
            } else if (button.id == 2) {
                if (currPage > 0) {
                    --currPage;
                }
            }
//            else if (button.id == 3)
//            {
//            	this.viewing = "";
//            	this.currPage = lastIndexPage;
//            	int i = 0;
//        		String lastCategory = "";
//        		boolean canDo = true;
//        		this.bookTotalPages = MathHelper.ceiling_float_int(SteamcraftRegistry.categories.size()/2F)+1;
//                this.updateButtons();
//            }

            if (button instanceof GuiButtonSelect) {
                GuiButtonSelect buttonSelect = (GuiButtonSelect) button;
                lastIndexPage = currPage;
                viewing = buttonSelect.name.substring(0, 1).equals("#") ? buttonSelect.name.substring(1) : buttonSelect.name;
                currPage = 0;
                bookTotalPages = MathHelper.ceiling_float_int(SteamcraftRegistry.researchPages.get(viewing).length / 2F);
                updateButtons();
                mustReleaseMouse = true;
            }

            initGui();
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1 && !viewing.isEmpty()) {
            viewing = "";
            currPage = lastIndexPage;
            bookTotalPages = MathHelper.ceiling_float_int(categories.size() / 2F) + 1;
            updateButtons();
        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        int d = Mouse.getDWheel();
        if (d < 0 && buttonNextPage.visible) {
            actionPerformed(buttonNextPage);
        }
        if (d > 0 && buttonPreviousPage.visible) {
            actionPerformed(buttonPreviousPage);
        }
        if (mustReleaseMouse && !Mouse.isButtonDown(0)) {
            mustReleaseMouse = false;
        }
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int k = (width - bookImageWidth) / 2;
        int b0 = (height - bookImageHeight) / 2;
        if (currPage == 0 && !viewing.isEmpty()) {
            mc.getTextureManager().bindTexture(book2);
            drawTexturedModalRect(k + 67, b0, 0, 0, bookImageWidth, bookImageHeight);
        } else {
            mc.getTextureManager().bindTexture(reverseBookGuiTextures);
            drawTexturedModalRect(k - 67, b0, 0, 0, bookImageWidth, bookImageHeight);
            mc.getTextureManager().bindTexture(bookGuiTextures);
            drawTexturedModalRect(k + 67, b0, 0, 0, bookImageWidth, bookImageHeight);
        }


        String s = book.getDisplayName();
        int l = fontRendererObj.getStringWidth(s);

        fontRendererObj.drawStringWithShadow(s, k + bookImageWidth / 2 - l / 2 - 3, b0 - 15, 0xFFFFFF);

        boolean unicode = fontRendererObj.getUnicodeFlag();
        fontRendererObj.setUnicodeFlag(true);
        if (!(currPage == 0 && !viewing.isEmpty())) {
            s = I18n.format("book.pageIndicator", (currPage - 1) * 2 + 4, bookTotalPages * 2);
            if (viewing.isEmpty()) {
                s = I18n.format("book.pageIndicator", (currPage - 1) * 2 + 2, bookTotalPages * 2 - 2);
            }

            l = fontRendererObj.getStringWidth(s);
            fontRendererObj.drawString(s, k - l + this.bookImageWidth - 44 + 67, b0 + 16, 0x3F3F3F);

            s = I18n.format("book.pageIndicator", (currPage - 1) * 2 + 3, bookTotalPages * 2);
            if (viewing.isEmpty()) {
                s = I18n.format("book.pageIndicator", (currPage - 1) * 2 + 1, bookTotalPages * 2 - 2);
            }

            fontRendererObj.drawString(s, k + l - this.bookImageWidth + 54 + 67, b0 + 16, 0x3F3F3F);
        } else {
            fontRendererObj.setUnicodeFlag(unicode);
            fontRendererObj.drawStringWithShadow(s, k + 67 + bookImageWidth / 2 - l / 2 - 3, b0 + 80, 0xC19E51);
            fontRendererObj.setUnicodeFlag(true);
            s = I18n.format("steamcraft.book.info");
            l = fontRendererObj.getStringWidth(s);
            fontRendererObj.drawString("\u00A7o" + s, k + 67 + bookImageWidth / 2 - l / 2 - 3, b0 + 90, 0xC19E51);
            s = I18n.format("steamcraft.book.by");
            l = fontRendererObj.getStringWidth(s + " " + mc.thePlayer.getDisplayNameString());
            fontRendererObj.drawString("\u00A7o" + s + " " + mc.thePlayer.getDisplayNameString(),
              k + 67 + bookImageWidth / 2 - l / 2 - 3, b0 + 105, 0xC19E51);
        }


        if (viewing.isEmpty()) {
            if (currPage > 0) {
                s = I18n.format("steamcraft.book.index");
                l = fontRendererObj.getStringWidth(s);
                fontRendererObj.drawString("\u00A7l" + "\u00A7n" + s, k - 67 + this.bookImageWidth / 2 - l / 2 - 5, b0 + 30, 0x3F3F3F);
                fontRendererObj.drawString("\u00A7l" + "\u00A7n" + s, k + 67 + this.bookImageWidth / 2 - l / 2 - 5, b0 + 30, 0x3F3F3F);

                ArrayList<GuiButtonSelect> thingsToRemove = new ArrayList<>();
                for (GuiButton button : buttonList) {
                    if (button instanceof GuiButtonSelect) {
                        thingsToRemove.add((GuiButtonSelect) button);
                    }
                }
                for (GuiButtonSelect button : thingsToRemove) {
                    this.buttonList.remove(button);
                }

                String category = categories.get((currPage - 1) * 2);
                int offset = 0;
                if (category.substring(category.length() - 1, category.length()).matches("-?\\d+")) {
                    offset = 9 * Integer.parseInt(category.substring(category.length() - 1, category.length()));
                    category = category.substring(0, category.length() - 1);
                }
                s = I18n.format(category);
                int i = 10;
                int offsetCounter = 0;
                fontRendererObj.drawString("\u00A7n" + s, k + 40 - 67, 44 + b0, 0x3F3F3F);
                for (MutablePair<String, String> research : SteamcraftRegistry.research) {
                    if (research.right.equals(category) && SteamcraftRegistry.researchPages.get(research.left).length > 0) {
                        offsetCounter++;
                        if (offsetCounter > offset && offsetCounter < offset + 10) {
                            s = research.left;
                            buttonList.add(new GuiButtonSelect(4, k + 50 - 67, b0 + 44 + i, 110, 10, s));
                            i += 10;
                        }
                    }
                }

                if (categories.size() > 1 + ((currPage - 1) * 2)) {
                    category = categories.get(((currPage - 1) * 2) + 1);
                    offset = 0;
                    offsetCounter = 0;
                    if (category.substring(category.length() - 1, category.length()).matches("-?\\d+")) {
                        offset = 9 * Integer.parseInt(category.substring(category.length() - 1, category.length()));
                        category = category.substring(0, category.length() - 1);
                    }
                    s = I18n.format(category);
                    i = 10;
                    this.fontRendererObj.drawString("\u00A7n" + s, k + 40 + 67, 44 + b0, 0x3F3F3F);
                    for (MutablePair<String, String> research : SteamcraftRegistry.research) {
                        if (research.right.equals(category) && SteamcraftRegistry.researchPages.get(research.left).length > 0) {
                            offsetCounter++;
                            if (offsetCounter > offset && offsetCounter < offset + 10) {
                                s = research.left;
                                buttonList.add(new GuiButtonSelect(4, k + 50 + 67, b0 + 44 + i, 110, 10, s));
                                i += 10;
                            }
                        }
                    }
                }
            }
            fontRendererObj.setUnicodeFlag(unicode);
            super.drawScreen(mouseX, mouseY, partialTicks);
        } else {
            fontRendererObj.setUnicodeFlag(unicode);
            super.drawScreen(mouseX, mouseY, partialTicks);
            fontRendererObj.setUnicodeFlag(true);
            if (SteamcraftRegistry.researchPages.containsKey(viewing)) {
                GL11.glEnable(GL11.GL_BLEND);
                BookPage[] pages = SteamcraftRegistry.researchPages.get(viewing);
                BookPage page = pages[(currPage) * 2];
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glPushMatrix();
                page.renderPage(k - 67, b0, fontRendererObj, this, itemRender, currPage == 0, mouseX, mouseY);
                GL11.glPopMatrix();
                BookPage originalPage = page;
                GL11.glEnable(GL11.GL_BLEND);
                if (pages.length > (currPage) * 2 + 1) {
                    page = pages[(currPage) * 2 + 1];
                    GL11.glPushMatrix();
                    page.renderPage(k + 67, b0, fontRendererObj, this, itemRender, false, mouseX, mouseY);
                    GL11.glPopMatrix();

                    page.renderPageAfter(k + 67, b0, fontRendererObj, this, itemRender, false, mouseX, mouseY);
                }
                originalPage.renderPageAfter(k - 67, b0, fontRendererObj, this, itemRender, currPage == 0, mouseX, mouseY);
            }
            fontRendererObj.setUnicodeFlag(unicode);
        }
    }

    public void renderToolTip(ItemStack stack0, int mouseX, int mouseY, boolean renderHyperlink) {
        List<String> list = stack0.getTooltip(mc.thePlayer, mc.gameSettings.advancedItemTooltips);
        this.zLevel = 1.0F;
        for (int k = 0; k < list.size(); ++k) {
            if (k == 0) {
                list.set(k, stack0.getRarity().rarityColor + list.get(k));
            } else {
                list.set(k, TextFormatting.GRAY + list.get(k));
            }
        }
        if (renderHyperlink) {
            for (ItemStack stack : SteamcraftRegistry.bookRecipes.keySet()) {
                if (stack.getItem() == stack0.getItem() && stack.getItemDamage() == stack0.getItemDamage()) {
                    list.add(TextFormatting.ITALIC + "" + TextFormatting.GRAY + I18n.format("steamcraft.book.clickme"));
                }
            }
        }

        FontRenderer font = stack0.getItem().getFontRenderer(stack0);
        this.drawHoveringText(list, mouseX, mouseY);
        drawHoveringText(list, mouseX, mouseY, (font == null ? fontRendererObj : font));
        this.zLevel = 0.0F;
    }

    public void renderText(String str, int mouseX, int mouseY) {
        List<String> list = new ArrayList<>();
        list.add(I18n.format(str));
        this.drawHoveringText(list, mouseX, mouseY);
        drawHoveringText(list, mouseX, mouseY, fontRendererObj);
    }

    public void itemClicked(ItemStack itemStack) {
         for (ItemStack stack : SteamcraftRegistry.bookRecipes.keySet()) {
            if (!mustReleaseMouse && stack.getItem() == itemStack.getItem() && stack.getItemDamage() == itemStack.getItemDamage()) {
                viewing = SteamcraftRegistry.bookRecipes.get(stack).left;
                currPage = MathHelper.floor_float((float) SteamcraftRegistry.bookRecipes.get(stack).right / 2.0F);
                bookTotalPages = MathHelper.ceiling_float_int(SteamcraftRegistry.researchPages.get(viewing).length / 2F);
                mustReleaseMouse = true;
                this.updateButtons();
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public static class NextPageButton extends GuiButton {
        private final boolean field_146151_o;

        public NextPageButton(int buttonID, int x, int y, boolean par4) {
            super(buttonID, x, y, 23, 13, "");
            this.field_146151_o = par4;
        }

        @Override
        public void drawButton(Minecraft minecraft, int mouseX, int mouseY) {
            if (this.visible) {
                boolean flag = mouseX >= xPosition && mouseY >= yPosition &&
                  mouseX < xPosition + width && mouseY < yPosition + height;
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                minecraft.getTextureManager().bindTexture(GuiSteamcraftBook.bookGuiTextures);
                int k = 0;
                int l = 192;

                if (flag) {
                    k += 23;
                }

                if (!this.field_146151_o) {
                    l += 13;
                }

                this.drawTexturedModalRect(xPosition, yPosition, k, l, 23, 13);
            }
        }
    }

    class GuiButtonSelect extends GuiButton {
        public String name;

        public GuiButtonSelect(int buttonID, int x, int y, int width, int height, String buttonText) {

            super(buttonID, x, y, width, height, buttonText.contains("#") ? I18n.format(buttonText.substring(1)) : I18n.format(buttonText));
            name = buttonText;
        }

        public void drawCenteredStringWithoutShadow(FontRenderer fontRenderer, String str, int x, int y, int color) {
            fontRenderer.drawString(str, x, y, color);
        }

        @Override
        public void drawButton(Minecraft mc, int mouseX, int mouseY) {
            if (this.visible) {
                FontRenderer fontRenderer = mc.fontRendererObj;
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                boolean test = mouseX >= xPosition && mouseY >= yPosition && mouseX < xPosition + width &&
                  mouseY < yPosition + height;
                this.mouseDragged(mc, mouseX, mouseY);
                int color = 0x3F3F3F;

                boolean unicode = fontRendererObj.getUnicodeFlag();
                fontRendererObj.setUnicodeFlag(true);
                this.drawCenteredStringWithoutShadow(fontRenderer, "\u2022 " + displayString, xPosition + (test ? 1 : 0),
                  yPosition + (this.height - 8) / 2, color);
                fontRendererObj.setUnicodeFlag(unicode);
            }
        }
    }

    /**
     * Opens the entry for the ItemStack
     * @param recipeStack The ItemStack to get the recipe entry for.
     * @param player The player opening the GUI.
     */
    public static void openRecipeFor(ItemStack recipeStack, EntityPlayer player) {
        MutablePair<String, Integer> page = SteamcraftRegistry.bookRecipes.get(recipeStack);
        viewing = page.left;
        currPage = SteamcraftRegistry.entriesWithSubEntries.contains(viewing) ? page.right / 2 : 0;
        lastIndexPage = 1;
        bookTotalPages = page.right;
        player.openGui(Steamcraft.instance, 1, player.worldObj, 0, 0, 0);
        ((GuiSteamcraftBook) Minecraft.getMinecraft().currentScreen).updateButtons();
    }
}
