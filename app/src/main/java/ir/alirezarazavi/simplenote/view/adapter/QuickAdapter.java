package ir.alirezarazavi.simplenote.view.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.CardView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.ArrayList;

import ir.alirezarazavi.simplenote.R;
import ir.alirezarazavi.simplenote.model.Note;

public class QuickAdapter extends BaseQuickAdapter<Note, BaseViewHolder> {
	
	
	private Context context;
	private String appTheme;
	
	public QuickAdapter(Context context, ArrayList<Note> allNotes, String appTheme) {
		super(R.layout.cardview, allNotes);
		this.context = context;
		this.appTheme = appTheme;
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
		
		helper.setText(R.id.itemTitle, item.getTitle());
		helper.setText(R.id.itemText, item.getText());
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
	
	
}
