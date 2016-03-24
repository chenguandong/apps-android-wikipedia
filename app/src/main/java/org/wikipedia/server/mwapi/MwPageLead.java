package org.wikipedia.server.mwapi;

import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import org.wikipedia.page.Page;
import org.wikipedia.page.PageProperties;
import org.wikipedia.page.PageTitle;
import org.wikipedia.page.Section;
import org.wikipedia.server.PageLead;
import org.wikipedia.server.PageLeadProperties;
import org.wikipedia.server.Protection;
import org.wikipedia.util.log.L;

import java.util.List;

import static org.wikipedia.util.StringUtil.capitalizeFirstChar;

/**
 * Gson POJO for loading the first stage of page content.
 */
public class MwPageLead implements PageLead {
    private MwServiceError error;
    private Mobileview mobileview;

    @Override
    public boolean hasError() {
        // if mobileview is not set something went terribly wrong
        return error != null || mobileview == null;
    }

    @Override
    @Nullable
    public MwServiceError getError() {
        return error;
    }

    @Override
    public void logError(String message) {
        if (error != null) {
            message += ": " + error.toString();
        }
        L.e(message);
    }

    /** Note: before using this check that #getMobileview != null */
    @Override
    public Page toPage(@NonNull PageTitle title) {
        return new Page(adjustPageTitle(title),
                mobileview.getSections(),
                mobileview.toPageProperties());
    }

    private PageTitle adjustPageTitle(@NonNull PageTitle title) {
        if (mobileview.getRedirected() != null) {
            // Handle redirects properly.
            title = new PageTitle(mobileview.getRedirected(), title.getSite(),
                    title.getThumbUrl());
        } else if (mobileview.getNormalizedTitle() != null) {
            // We care about the normalized title only if we were not redirected
            title = new PageTitle(mobileview.getNormalizedTitle(), title.getSite(),
                    title.getThumbUrl());
        }
        title.setDescription(mobileview.getDescription());
        return title;
    }

    @Override
    public String getLeadSectionContent() {
        if (mobileview != null) {
            return mobileview.getSections().get(0).getContent();
        } else {
            return "";
        }
    }

    @Nullable
    @Override
    public String getTitlePronunciationUrl() {
        return null;
    }

    @Nullable
    @Override
    public Location getGeo() {
        return null;
    }

    @VisibleForTesting
    public Mobileview getMobileview() {
        return mobileview;
    }


    /**
     * Almost everything is in this inner class.
     */
    public static class Mobileview implements PageLeadProperties {
        private int id;
        private long revision;
        @Nullable private String lastmodified;
        @Nullable private String displaytitle;
        @Nullable private String redirected;
        @Nullable private String normalizedtitle;
        private int languagecount;
        private boolean editable;
        private boolean mainpage;
        private boolean disambiguation;
        @Nullable private String description;
        @Nullable private Image image;
        @Nullable private Thumb thumb;
        @Nullable private Protection protection;
        @Nullable private List<Section> sections;

        /** Converter */
        public PageProperties toPageProperties() {
            return new PageProperties(this);
        }

        @Override
        public int getId() {
            return id;
        }

        @Override
        public long getRevision() {
            return revision;
        }

        @Override
        @Nullable
        public String getLastModified() {
            return lastmodified;
        }

        @Override
        public int getLanguageCount() {
            return languagecount;
        }

        @Override
        @Nullable
        public String getDisplayTitle() {
            return displaytitle;
        }

        @Override
        @Nullable
        public String getTitlePronunciationUrl() {
            return null;
        }

        @Override
        @Nullable
        public Location getGeo() {
            return null;
        }

        @Override
        @Nullable
        public String getRedirected() {
            return redirected;
        }

        @Override
        @Nullable
        public String getNormalizedTitle() {
            return normalizedtitle;
        }

        @Override
        @Nullable
        public String getDescription() {
            return description != null ? capitalizeFirstChar(description) : null;
        }

        @Override
        @Nullable
        public String getLeadImageUrl() {
            return thumb != null ? thumb.getUrl() : null;
        }

        @Override
        @Nullable
        public String getLeadImageName() {
            return image != null ? image.getFile() : null;
        }

        @Override
        @Nullable
        public String getFirstAllowedEditorRole() {
            return protection != null ? protection.getFirstAllowedEditorRole() : null;
        }

        @Override
        public boolean isEditable() {
            return editable;
        }

        @Override
        public boolean isMainPage() {
            return mainpage;
        }

        @Override
        public boolean isDisambiguation() {
            return disambiguation;
        }

        @Override
        @Nullable
        public List<Section> getSections() {
            return sections;
        }
    }


    /**
     * For the lead image File: page name
     */
    public static class Image {
        private String file;

        public String getFile() {
            return file;
        }
    }

    /**
     * For the lead image URL
     */
    public static class Thumb {
        private String url;

        public String getUrl() {
            return url;
        }
    }
}