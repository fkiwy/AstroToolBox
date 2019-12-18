package astro.tool.box.module;

public class HtmlText {

    private final StringBuilder builder;

    public HtmlText() {
        builder = new StringBuilder("<font face=\"\" size=\"3\">");
    }

    public HtmlText(String font, int size) {
        builder = new StringBuilder("<font face=\"" + font + "\" size=\"" + size + "\">");
    }

    public HtmlText boldRow(String text) {
        return boldText(text).newLine();
    }

    public HtmlText row(String text) {
        return text(text).newLine();
    }

    public HtmlText boldText(String text) {
        builder.append("<b>").append(text).append("</b>");
        return this;
    }

    public HtmlText text(String text) {
        builder.append(text);
        return this;
    }

    public HtmlText newLine() {
        builder.append("<br/>");
        return this;
    }

    @Override
    public String toString() {
        builder.append("</font>");
        return builder.toString();
    }

}
