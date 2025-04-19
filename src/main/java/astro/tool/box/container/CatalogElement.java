package astro.tool.box.container;

import java.util.Comparator;
import java.util.Objects;

import astro.tool.box.enumeration.Alignment;

public class CatalogElement {

    private String name;

    private String value;

    private Alignment alignment;

    private Comparator<String> comparator;

    private boolean onFocus;

    private boolean computed;

    private boolean faulty;

    private String toolTip;

    public CatalogElement() {
    }

    public CatalogElement(String name, String value, Alignment alignment, Comparator<String> comparator) {
        this.name = name;
        this.value = value;
        this.alignment = alignment;
        this.comparator = comparator;
    }

    public CatalogElement(String name, String value, Alignment alignment, Comparator<String> comparator, String toolTip) {
        this.name = name;
        this.value = value;
        this.alignment = alignment;
        this.comparator = comparator;
        this.toolTip = toolTip;
    }

    public CatalogElement(String name, String value, Alignment alignment, Comparator<String> comparator, boolean onFocus) {
        this.name = name;
        this.value = value;
        this.alignment = alignment;
        this.comparator = comparator;
        this.onFocus = onFocus;
    }

    public CatalogElement(String name, String value, Alignment alignment, Comparator<String> comparator, boolean onFocus, boolean computed) {
        this.name = name;
        this.value = value;
        this.alignment = alignment;
        this.comparator = comparator;
        this.onFocus = onFocus;
        this.computed = computed;
    }

    public CatalogElement(String name, String value, Alignment alignment, Comparator<String> comparator, boolean onFocus, boolean computed, boolean faulty) {
        this.name = name;
        this.value = value;
        this.alignment = alignment;
        this.comparator = comparator;
        this.onFocus = onFocus;
        this.computed = computed;
        this.faulty = faulty;
    }

    @Override
    public String toString() {
        return "CatalogElement{" + "name=" + name + ", value=" + value + ", alignment=" + alignment + ", comparator=" + comparator + ", onFocus=" + onFocus + ", computed=" + computed + ", faulty=" + faulty + ", toolTip=" + toolTip + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.name);
        hash = 89 * hash + Objects.hashCode(this.value);
        hash = 89 * hash + Objects.hashCode(this.alignment);
        hash = 89 * hash + Objects.hashCode(this.comparator);
        hash = 89 * hash + (this.onFocus ? 1 : 0);
        hash = 89 * hash + (this.computed ? 1 : 0);
        hash = 89 * hash + (this.faulty ? 1 : 0);
        hash = 89 * hash + Objects.hashCode(this.toolTip);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        final CatalogElement other = (CatalogElement) obj;
        if (this.onFocus != other.onFocus) {
            return false;
        }
        if (this.computed != other.computed) {
            return false;
        }
        if (this.faulty != other.faulty) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.value, other.value)) {
            return false;
        }
        if (!Objects.equals(this.toolTip, other.toolTip)) {
            return false;
        }
        if (this.alignment != other.alignment) {
            return false;
        }
        return Objects.equals(this.comparator, other.comparator);
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public Alignment getAlignment() {
        return alignment;
    }

    public Comparator<String> getComparator() {
        return comparator;
    }

    public boolean isOnFocus() {
        return onFocus;
    }

    public boolean isComputed() {
        return computed;
    }

    public boolean isFaulty() {
        return faulty;
    }

    public String getToolTip() {
        return toolTip;
    }

}
