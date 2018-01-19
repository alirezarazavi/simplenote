package ir.alirezarazavi.simplenote;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import ir.alirezarazavi.simplenote.model.NoteInteractor;
import ir.alirezarazavi.simplenote.model.NoteInteractorImpl;

@Module
public class AppModule {
	App app;
	public AppModule(App app) {
		this.app = app;
	}
	
	@Provides @Singleton
	Context providesApplicationContext() {
		return app;
	}
	
	@Provides @Singleton
	SharedPreferences providesSharedPreferences(Context app) {
		return PreferenceManager.getDefaultSharedPreferences(app);
	}
	
	@Provides @Singleton
	NoteInteractor providesNoteInteractor() {
		return new NoteInteractorImpl(app.getApplicationContext());
	}
	
	
}
