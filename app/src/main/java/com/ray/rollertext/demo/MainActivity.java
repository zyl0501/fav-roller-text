package com.ray.rollertext.demo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.ray.rollertext.FavImage;
import com.ray.rollertext.RollerText;

public class MainActivity extends AppCompatActivity {
    int number;
    boolean isFav;

    EditText numberEdt;
    RadioGroup selRadioGroup;
    Button setBtn;
    ViewGroup favLayout;
    RollerText rollerText;
    FavImage favImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        numberEdt = findViewById(R.id.number);
        selRadioGroup = findViewById(R.id.radio_group);
        setBtn = findViewById(R.id.ok_btn);
        favLayout = findViewById(R.id.fav_layout);
        rollerText = findViewById(R.id.text);
        favImage = findViewById(R.id.fav_img);

        setBtn.performClick();
    }

    public void onSetFav(View view) {
        int id = selRadioGroup.getCheckedRadioButtonId();
        isFav = id == R.id.sel_radio;
        number = TextUtils.isEmpty(numberEdt.getText()) ? 0 : Integer.parseInt(numberEdt.getText().toString());

        favImage.setSelected(isFav, false);
        rollerText.setText(String.valueOf(number), false);
    }

    public void onFavLayoutClick(View view) {
        if (isFav) {
            isFav = false;
            number -= 1;
        } else {
            isFav = true;
            number += 1;
        }
        rollerText.setText(String.valueOf(number));
        favImage.setSelected(isFav);
    }

    public void onFavImgClick(View view) {
        favLayout.performClick();
    }

    public void onRollerTextClick(View view) {
        favLayout.performClick();
    }

}
