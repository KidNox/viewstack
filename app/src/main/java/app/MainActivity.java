package app;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import viewstack.ViewComponent;
import viewstack.ViewStack;
import viewstack.sample.R;

public class MainActivity extends AppCompatActivity {

    ViewStack viewStack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewStack = ViewStack.of(this, savedInstanceState);
        if(viewStack.isEmpty()) {
            viewStack.show(new MyViewComponent());
        }
    }

    @Override
    public void onBackPressed() {
        if(!viewStack.handleBack()) {
            super.onBackPressed();
        }
    }

    static class MyViewComponent extends ViewComponent {

        int index;

        @Override
        public void onCreate() {
            log("create");
            index = getArguments().getInt("index");
        }

        @Override
        public View render(LayoutInflater inflater, ViewGroup parent) {
            log("render");
            return inflater.inflate(R.layout.component_test, parent, false);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void afterRender(View view) {
            log("afterRender");
            TextView textView = view.findViewById(R.id.text);
            textView.setText("component " + index);
            textView.setOnClickListener(v -> showNextComponent());
        }

        void showNextComponent() {
            MyViewComponent component = new MyViewComponent();
            component.getArguments().putInt("index", index + 1);
            if(hasContext()) {
                getViewStack().show(component);
            }
        }

        @Override
        public void onAttach() {
            log("onAttach");
        }

        @Override
        public void onDetach(boolean removedFromStack) {
            log("onDetach");
        }

        @Override
        public void onDestroyView() {
            log("onDestroyView");
        }

        @Override
        public void onDestroy() {
            log("destroy");
        }

        private void log(String method) {
            Log.d(method, this.toString());
        }

        @Override
        public String toString() {
            return "component " + getArguments().getInt("index");
        }
    }

}
