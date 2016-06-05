/*
 * Copyright (C) 2015-2016 Daniel Schaal <daniel@schaal.email>
 *
 * This file is part of OCReader.
 *
 * OCReader is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OCReader is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OCReader.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package email.schaal.ocreader.view;

import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import email.schaal.ocreader.R;
import email.schaal.ocreader.model.Feed;
import email.schaal.ocreader.model.Item;
import email.schaal.ocreader.util.FaviconUtils;
import email.schaal.ocreader.util.FeedColors;
import email.schaal.ocreader.util.StringUtils;

/**
 * RecyclerView.ViewHolder to display a feed Item.
 */
public class ItemViewHolder extends RecyclerView.ViewHolder implements FaviconUtils.PaletteBitmapAsyncListener {
    private static final String TAG = ItemViewHolder.class.getName();

    private final OnClickListener clickListener;

    @ColorInt private final int defaultFeedTextColor;

    private final TextView textViewTitle;
    private final TextView textViewFeedTitle;
    private final TextView textViewTime;
    private final ImageView faviconImageView;
    private final ImageView starImageView;

    private final View[] alphaViews;

    public ItemViewHolder(final View itemView, final OnClickListener clickListener) {
        super(itemView);
        this.clickListener = clickListener;

        textViewTitle = (TextView) itemView.findViewById(R.id.textViewTitle);
        textViewFeedTitle = (TextView) itemView.findViewById(R.id.textViewFeedTitle);
        textViewTime = (TextView) itemView.findViewById(R.id.textViewTime);

        faviconImageView = (ImageView) itemView.findViewById(R.id.imageview_favicon);
        starImageView = (ImageView) itemView.findViewById(R.id.imageview_star);

        TypedArray typedArray = itemView.getContext().obtainStyledAttributes(new int[] { android.R.attr.textColorSecondary });
        try {
            defaultFeedTextColor = typedArray.getColor(0, 0);
        } finally {
            typedArray.recycle();
        }

        alphaViews = new View[] {
                textViewTitle,
                textViewFeedTitle,
                textViewTime,
                faviconImageView,
                starImageView
        };
    }

    public void bindItem(final Item item, final int position, boolean selected) {
        textViewTitle.setText(item.getTitle());

        Feed feed = item.getFeed();
        if(feed != null) {
            textViewFeedTitle.setText(feed.getTitle());
        } else {
            Log.w(TAG, "Feed == null");
            textViewFeedTitle.setText("");
        }

        textViewTime.setText(StringUtils.getTimeSpanString(itemView.getContext(), item.getPubDate()));

        faviconImageView.setImageResource(R.drawable.ic_feed_icon);
        textViewFeedTitle.setTextColor(defaultFeedTextColor);

        FaviconUtils.getInstance().loadFavicon(itemView, feed, this);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickListener.onItemClick(item, position);
            }
        });

        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                clickListener.onItemLongClick(item, position);
                return true;
            }
        });
        setUnreadState(item.isUnread());
        setStarredState(item.isStarred());
        if (selected) {
            itemView.setBackgroundResource(R.drawable.item_background);
        } else {
            int[] attrs = new int[]{R.attr.selectableItemBackground};
            TypedArray typedArray = itemView.getContext().obtainStyledAttributes(attrs);
            int backgroundResource = typedArray.getResourceId(0, 0);
            itemView.setBackgroundResource(backgroundResource);
            typedArray.recycle();
        }
    }

    private void setUnreadState(boolean unread) {
        float alpha = unread ? 1.0f : 0.5f;
        for(View view: alphaViews) {
            view.setAlpha(alpha);
        }
    }

    private void setStarredState(boolean starred) {
        starImageView.setVisibility(starred ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onGenerated(@Nullable FeedColors palette, @Nullable Drawable favicon) {
        faviconImageView.setImageDrawable(favicon);
        textViewFeedTitle.setTextColor(FeedColors.get(palette, FeedColors.Type.TEXT, defaultFeedTextColor));
    }

    public interface OnClickListener {
        void onItemClick(Item item, int position);
        void onItemLongClick(Item item, int position);
    }
}
