package app;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import hugo.weaving.DebugLog;
import viewstack.ViewComponent;
import viewstack.ViewStack;
import viewstack.sample.R;
import viewstack.utils.StackOptions;
import viewstack.utils.StackChangedListener;

public class MainActivity extends AppCompatActivity {

    ViewStack viewStack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewStack = ViewStack.of(this, savedInstanceState, new StackOptions()
                /*.ignoreBackPressedOnTransaction()*/
                .withDefaultAnimationDelegate(new SlideTransitionDelegate())
                .withStackChangedListener(new StackChangedListener() {
                    @DebugLog
                    @Override
                    public void onComponentAdded(ViewComponent component) {
                    }
                    @DebugLog
                    @Override
                    public void onComponentRemoved(ViewComponent component) {
                    }
                }));
        if (viewStack.isEmpty()) {
            viewStack.show(new MyViewComponent());
        }
    }

    @Override
    public void onBackPressed() {
        if (!viewStack.handleBack()) {
            super.onBackPressed();
        }
    }

    static class MyViewComponent extends ViewComponent {

        static final String[] colors = new String[]{"#f44336", "#9C27B0", "#3F51B5", "#009688", "#CDDC39", "#FFC107"};

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
            view.setOnClickListener(v -> showNextComponent());
            view.setBackgroundColor(getColor());
            TextView textView = view.findViewById(R.id.text);
            textView.setText("component " + index);
        }

        private int getColor() {
            return Color.parseColor(colors[index % 6]);
        }

        void showNextComponent() {
            if (isActive()) {
                MyViewComponent component = new MyViewComponent();
                component.getArguments().putInt("index", index + 1);
                log("show component " + (index + 1));
                getViewStack().show(component);
            }
        }

        @Override
        public void onAttach() {
            log("onAttach");
        }

        @Override
        public void onDetach(boolean removedFromStack) {
            log("onDetach " + removedFromStack);
        }

        @Override
        public void onDestroyView() {
            log("onDestroyView");
        }

        @Override
        public void onDestroy(boolean removedFromStack) {
            log("destroy " + removedFromStack);
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
