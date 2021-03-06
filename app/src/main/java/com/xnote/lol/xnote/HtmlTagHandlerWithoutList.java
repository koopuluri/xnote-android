package com.xnote.lol.xnote;

import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.style.TypefaceSpan;

import org.xml.sax.XMLReader;

import java.util.Vector;

/**
 * By mohammedlakkadshaw from: https://gist.github.com/mlakkadshaw/5983704
 * with github handle: mlakkadshaw
 * Adds spans for tags in html that aren't by default handled by Html.fromHtml(...).
 */
public class HtmlTagHandlerWithoutList implements Html.TagHandler {
    public static final String TAG = "HtmlTagHandler";
    private int mListItemCount = 0;
    private Vector<String> mListParents = new Vector<String>();

    @Override
    public void handleTag(final boolean opening, final String tag, Editable output, final XMLReader xmlReader) {
        if (tag.equals("ul") || tag.equals("ol") || tag.equals("dd")) {
            if (opening) {
                mListParents.add(tag);
            } else mListParents.remove(tag);

            mListItemCount = 0;
        } else if (tag.equals("li") && !opening) {
            // do nothing (why? read the name of the class!).
        } else if(tag.equalsIgnoreCase("img")) {
            //if(opening) {
            output.append("\n");
            //}
        }
        else if(tag.equalsIgnoreCase("code")) {
            if(opening) {
                output.setSpan(new TypefaceSpan("monospace"), output.length(), output.length(), Spannable.SPAN_MARK_MARK);
            } else {
                Object obj = getLast(output, TypefaceSpan.class);
                int where = output.getSpanStart(obj);
                output.setSpan(new TypefaceSpan("monospace"), where, output.length(), 0);
            }
        }
    }

    private Object getLast(Editable text, Class kind) {
        Object[] objs = text.getSpans(0, text.length(), kind);
        if(objs.length == 0) {
            return null;
        } else {
            for (int i=objs.length; i > 0; i--) {
                if(text.getSpanFlags(objs[i-1]) == Spannable.SPAN_MARK_MARK) {
                    return objs[i-1];
                }
            }
            return null;
        }
    }
}