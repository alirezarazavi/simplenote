package ir.alirezarazavi.simplenote;

import android.app.Application;
import android.content.Context;

import com.onesignal.OneSignal;

import ir.alirezarazavi.simplenote.DaggerAppComponent;

public class App extends Application {
	AppComponent appComponent;
	
	@Override
	public void onCreate() {
		super.onCreate();
		OneSignal.startInit(this).init();
		
		appComponent = DaggerAppComponent.builder().appModule(new AppModule(this)).build();
		appComponent.inject(this);
	}
	
	public AppComponent getAppComponent() {
		return appComponent;
	}
	
	public static AppComponent getAppComponent(Context context) {
		App app = (App) context.getApplicationContext();
		if (app.appComponent == null) {
			app.appComponent = app.getAppComponent();
		}
		return app.appComponent;
	}
	
}
