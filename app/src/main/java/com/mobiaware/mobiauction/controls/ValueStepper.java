/*
 * Copyright (c) 2010 mobiaware.com.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.mobiaware.mobiauction.controls;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mobiaware.mobiauction.R;

import java.text.NumberFormat;
import java.util.Locale;

public class ValueStepper extends LinearLayout {
    private static final long REPEAT_DELAY = 50;

    private static final int MINIMUM = 0;
    private static final int MAXIMUM = 999;
    private static final int STEP = 1;

    private Button _decrementBtn;
    private Button _incrementBtn;
    private TextView _valueText;

    private Handler repeatUpdateHandler = new Handler();

    public double _value;

    private boolean _autoIncrement;
    private boolean _autoDecrement;

    private double _minimum = MINIMUM;
    private double _maximum = MAXIMUM;
    private double _step = STEP;

    class RepetetiveUpdater implements Runnable {
        public void run() {
            if (_autoIncrement) {
                increment();
                repeatUpdateHandler.postDelayed(new RepetetiveUpdater(), REPEAT_DELAY);
            } else if (_autoDecrement) {
                decrement();
                repeatUpdateHandler.postDelayed(new RepetetiveUpdater(), REPEAT_DELAY);
            }
        }
    }

    public ValueStepper(Context context, AttributeSet attrs) {
        super(context, attrs);

        initControl(context);
    }

    public void setMinimum(double minimum) {
        _minimum = minimum;
    }

    public void setMaximum(double maximum) {
        _maximum = maximum;
    }

    public void setStep(double step) {
        _step = step;
    }

    public double getValue() {
        return _value;
    }

    public void setValue(double value) {
        if (value > _maximum) {
            value = MAXIMUM;
        }

        if (value >= 0) {
            _value = value;
            updateTextField();
        }
    }

    private void initControl(Context context) {
        LayoutInflater layoutInflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.control_value_stepper, this);

        initDecrementButton(view);
        initValueText(view);
        initIncrementButton(view);
    }

    private void initDecrementButton(View view) {
        _decrementBtn = (Button) view.findViewById(R.id.btn_minus);

        _decrementBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                decrement();
            }
        });


        _decrementBtn.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View arg0) {
                _autoDecrement = true;
                repeatUpdateHandler.post(new RepetetiveUpdater());
                return false;
            }
        });

        _decrementBtn.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP && _autoDecrement) {
                    _autoDecrement = false;
                }
                return false;
            }
        });
    }

    private void initValueText(View view) {
        _value = _minimum;

        _valueText = (TextView) view.findViewById(R.id.edit_text);
        updateTextField();
    }

    private void initIncrementButton(View view) {
        _incrementBtn = (Button) view.findViewById(R.id.btn_plus);

        _incrementBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                increment();
            }
        });

        _incrementBtn.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View arg0) {
                _autoIncrement = true;
                repeatUpdateHandler.post(new RepetetiveUpdater());
                return false;
            }
        });

        _incrementBtn.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP && _autoIncrement) {
                    _autoIncrement = false;
                }
                return false;
            }
        });
    }

    private void increment() {
        if (_value < _maximum) {
            _value = _value + _step;
            updateTextField();
        }
    }

    private void decrement() {
        if (_value > _minimum) {
            _value = _value - _step;
            updateTextField();
        }
    }

    private void updateTextField() {
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
        _valueText.setText(format.format(_value));
    }
}
