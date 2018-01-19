package ir.alirezarazavi.simplenote;

import javax.inject.Singleton;

import dagger.Component;
import ir.alirezarazavi.simplenote.view.activity.MainActivity;
import ir.alirezarazavi.simplenote.view.activity.PinActivity;
import ir.alirezarazavi.simplenote.view.activity.SearchActivity;
import ir.alirezarazavi.simplenote.view.activity.SettingsActivity;
import ir.alirezarazavi.simplenote.view.fragment.ListFragment;
import ir.alirezarazavi.simplenote.view.fragment.NoteFragment;

@Singleton @Component(modules = {AppModule.class})
public interface AppComponent {
	void inject(App app);
	void inject(MainActivity activity);
	void inject(ListFragment listFragment);
	void inject(NoteFragment noteFragment);
	void inject(SearchActivity searchActivity);
	void inject(SettingsActivity settingsActivity);
	void inject(PinActivity pinActivity);
}
