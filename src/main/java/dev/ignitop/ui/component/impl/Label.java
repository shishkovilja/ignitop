package dev.ignitop.ui.component.impl;

import dev.ignitop.ui.component.TerminalComponent;
import org.fusesource.jansi.Ansi;

/**
 * Complex label class. Can be build phrase-by-phrase (whitespace separated). Each phrase can have own styling.
 */
public class Label implements TerminalComponent {
    /** Text. */
    private final String text;

    /**
     * @param text Text.
     */
    private Label(String text) {
        this.text = text;
    }

    /**
     * Add bold text.
     *
     * @param obj Object.
     */
    public static Label.Builder bold(Object obj) {
        return new Builder().bold(obj);
    }

    /**
     * Add ordinary text.
     *
     * @param obj Object.
     */
    public static Builder normal(Object obj) {
        return new Builder().normal(obj);
    }

    /**
     * Add underlined text.
     *
     * @param obj Object.
     */
    public static Builder underline(Object obj) {
        return new Builder().underline(obj);
    }

    /** {@inheritDoc} */
    @Override public void render(int width) {
        System.out.println(text.length() > width ? text.substring(0, width) : text);
    }

    /** {@inheritDoc} */
    @Override public int contentWidth() {
        return text.length();
    }

    /**
     * Builder for label.
     */
    @SuppressWarnings("PublicInnerClass")
    public static final class Builder {
        /** Ansi. */
        private final Ansi ansi = Ansi.ansi();

        /** Emptiness marker. */
        private boolean empty = true;

        /** */
        private void addWhitespaceIfNecessary() {
            if (!empty)
                ansi.a(' ');
            else
                empty = false;
        }

        /** */
        public Label build() {
            return new Label(ansi.toString());
        }

        /** */
        public Builder bold(Object obj) {
            addWhitespaceIfNecessary();

            ansi.bold()
                .a(obj.toString())
                .reset();

            return this;
        }

        /** */
        public Builder normal(Object obj) {
            addWhitespaceIfNecessary();

            ansi.a(obj.toString());

            return this;
        }

        /** */
        public Builder underline(Object obj) {
            addWhitespaceIfNecessary();

            ansi.a(Ansi.Attribute.UNDERLINE)
                .a(obj.toString())
                .reset();

            return this;
        }
    }
}
