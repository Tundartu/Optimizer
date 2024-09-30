package com.example.optimization;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class InstructionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instruction);

        TextView instructionText = findViewById(R.id.instructionText);

        String instruction = "Инструкция по вводу математических функций...\n\n"
                + "Примеры:\n"
                + "1. Сложение: + (Пример x + 2)\n"
                + "2. Вычитание: - (Пример: x - 5)\n"
                + "3. Умножение: * (Пример: 3 * x)\n"
                + "4. Деление: / (Пример: x / 2)\n"
                + "5. Степень: ^ (Пример: x^3 — x в кубе)\n"
                + "Математические функции:\n"
                + "Экспонента: exp(x) (Пример: exp(-x))\n"
                + "Логарифм: ln(x) (Пример: ln(x))\n"
                + "Косинус: cos(x) (Пример: cos(x))\n"
                + "Синус: sin(x) (Пример: sin(x))\n"
                + "Тангенс tan(x) (Пример: tan(x))\n"
                + "Абсолютное значение: abs(x) (Пример: abs(x-3))\n"
                + "При вводе числа PI в границы диапазона его необходимо рассчитать вручную (Например: 2 * Pi = 2 * 3.14159 = 6.28318530718) "
                + "Используйте правильные скобки для более сложных выражений, например: sin(x) + cos(x) / (x^2 + 1)"
                ;

        instructionText.setText(instruction);
    }
}
