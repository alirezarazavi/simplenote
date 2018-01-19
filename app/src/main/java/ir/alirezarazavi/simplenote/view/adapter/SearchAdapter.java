package ir.alirezarazavi.simplenote.view.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.CardView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.TextAppearanceSpan;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.ArrayList;

import ir.alirezarazavi.simplenote.R;
import ir.alirezarazavi.simplenote.model.Note;

public class SearchAdapter extends BaseQuickAdapter<Note, BaseViewHolder> {
	
	
	private Context context;
	private String appTheme;
	private String searchKeyword;
	
	public SearchAdapter(Context context, ArrayList<Note> allNotes, String appTheme, String search_keyword) {
		super(R.layout.cardview, allNotes);
		this.context = context;
		this.appTheme = appTheme;
		searchKeyword = search_keyword;
	}
	
	
	@Override
	protected void convert(BaseViewHolder helper, Note item) {
		CardView cardView = helper.getView(R.id.cardView);
		TextView itemTitle = helper.getView(R.id.itemTitle);
		TextView itemText = helper.getView(R.id.itemText);
		// Splitting date and time, to show only date in list
		String fullDate = item.getDate();
		String[] splitDate;
		String delimiter = "\n";
		splitDate = fullDate.split(delimiter);
		
		// highlight search keyword
		if (highlightSearchKeyword(item.getTitle()) != null) {
			helper.setText(R.id.itemTitle, highlightSearchKeyword(item.getTitle()));
		} else {
			helper.setText(R.id.itemTitle, item.getTitle());
		}
		
		if (highlightSearchKeyword(item.getText()) != null) {
			helper.setText(R.id.itemText, highlightSearchKeyword(item.getText()));
		} else {
			helper.setText(R.id.itemText, item.getText());
		}
		
		//helper.setText(R.id.itemDate, item.getDate());
		helper.setText(R.id.itemDate, splitDate[0]);
		
		// set settings app theme
		if (appTheme.equals("1")) {
			cardView.setCardBackgroundColor(context.getResources().getColor(R.color.dark_bg_color));
			itemTitle.setTextColor(context.getResources().getColor(R.color.title_dark));
			itemText.setTextColor(context.getResources().getColor(R.color.text_dark));
		} else {
			cardView.setCardBackgroundColor(context.getResources().getColor(R.color.cardview_light_background));
		}
		
	}
	
	private Spannable highlightSearchKeyword(String text) {
		int startPos = text.indexOf(searchKeyword);
		int endPos = startPos + searchKeyword.length();
		if (startPos != -1) {
			Spannable spannable = new SpannableString(text);
			int color = Color.BLUE;
			if (appTheme.equals("1")) {
				color = Color.RED;
			}
			ColorStateList textColor = new ColorStateList(new int[][]{new int[]{}}, new int[]{color});
			TextAppearanceSpan highlightSpan = new TextAppearanceSpan(null, Typeface.BOLD, -1, textColor, null);
			spannable.setSpan(highlightSpan, startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			return spannable;
		} else {
			return null;
		}
	}
	
	
}
