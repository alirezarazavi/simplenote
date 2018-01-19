package ir.alirezarazavi.simplenote.presenter;

public interface BasePresenter<T> {
	
	void bind(T view);
	void unbind();
	void updateUi();
	
}
