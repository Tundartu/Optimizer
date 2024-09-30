package com.example.optimization;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.mariuszgromada.math.mxparser.Argument;
import org.mariuszgromada.math.mxparser.Expression;

public class MainActivity extends AppCompatActivity {

    private EditText functionInput, lowerBoundInput, upperBoundInput;
    private RadioGroup optimizationTypeGroup;
    private Button optimizeButton;
    private TextView resultView;
    private boolean isMaximization;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        functionInput = findViewById(R.id.functionInput);
        lowerBoundInput = findViewById(R.id.lowerBoundInput);
        upperBoundInput = findViewById(R.id.upperBoundInput);
        optimizationTypeGroup = findViewById(R.id.optimizationTypeGroup);
        optimizeButton = findViewById(R.id.optimizeButton);
        resultView = findViewById(R.id.resultView);
        Button instructionButton = findViewById(R.id.instructionButton);
        optimizationTypeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            isMaximization = checkedId == R.id.maximizeOption;
        });
        instructionButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, InstructionActivity.class);
            startActivity(intent);

        });
        optimizeButton.setOnClickListener(v -> optimizeFunction());
    }


    private void optimizeFunction() {
        String functionStr = functionInput.getText().toString();
        String lowerStr = lowerBoundInput.getText().toString();
        String upperStr = upperBoundInput.getText().toString();

        if (functionStr.isEmpty() || lowerStr.isEmpty() || upperStr.isEmpty()) {
            Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        double lowerBound = Double.parseDouble(lowerStr);
        double upperBound = Double.parseDouble(upperStr);

        // Создание объекта функции
        Argument x = new Argument("x");
        Expression expression = new Expression(functionStr, x);

        // Проверка корректности функции
        if (!expression.checkSyntax()) {
            resultView.setText("Ошибка в функции: " + expression.getErrorMessage());
            return;
        }

        // Проверка наличия производной
        boolean hasDerivative = hasDerivative(expression, x, lowerBound, upperBound);

        // Выбор метода оптимизации
        if (hasDerivative) {
            // Проверка гладкости
            boolean isSmooth = isFunctionSmooth(expression, x, lowerBound, upperBound);

            if (isSmooth) {
                // Проверка выпуклости
                int convexity = checkConvexity(expression, x, lowerBound, upperBound);
                // convexity: 1 - строго выпуклая, 0 - выпуклая, -1 - невыпуклая

                if (convexity == 1) {
                    // Строго выпуклая функция
                    performGradientDescent(expression, x, lowerBound, upperBound);
                } else {
                    // Выпуклая или невыпуклая функция
                    performGoldenSectionSearch(expression, x, lowerBound, upperBound);
                }
            } else {
                // Функция не гладкая
                performGoldenSectionSearch(expression, x, lowerBound, upperBound);
            }
        } else {
            // Нет производной
            performFibonacciSearch(expression, x, lowerBound, upperBound);
        }

    }


    private boolean isFunctionSmooth(Expression expression, Argument x, double lower, double upper) {
        try {
            double delta = 1e-5;
            for (double xi = lower; xi <= upper; xi += (upper - lower) / 10) {
                x.setArgumentValue(xi - delta);
                double f0 = expression.calculate();

                x.setArgumentValue(xi + delta);
                double f1 = expression.calculate();

                if (Math.abs(f1 - f0) > 1e3) { // Проверяем на резкие скачки
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    private int checkConvexity(Expression expression, Argument x, double lower, double upper) {
        try {
            double delta = 1e-5;
            boolean isConvex = true;
            boolean isStrictlyConvex = true;

            for (double xi = lower; xi <= upper; xi += (upper - lower) / 10) {
                // Вычисление второй производной численно
                x.setArgumentValue(xi - delta);
                double f1 = expression.calculate();

                x.setArgumentValue(xi);
                double f2 = expression.calculate();

                x.setArgumentValue(xi + delta);
                double f3 = expression.calculate();

                double secondDerivative = (f1 - 2 * f2 + f3) / (delta * delta);

                if (secondDerivative < 0) {
                    isConvex = false;
                    break;
                } else if (secondDerivative == 0) {
                    isStrictlyConvex = false;
                }
            }

            if (isConvex) {
                if (isStrictlyConvex) {
                    return 1; // Строго выпуклая
                } else {
                    return 0; // Выпуклая
                }
            } else {
                return -1; // Невыпуклая
            }
        } catch (Exception e) {
            return -1;
        }
    }


    private void performGradientDescent(Expression expression, Argument x, double lower, double upper) {
        double xi = (lower + upper) / 2; // Начальное приближение
        double learningRate = 0.01; // Скорость обучения
        double tolerance = 1e-6; // Допустимая точность
        int maxIterations = 1000; // Максимальное количество итераций
        int iteration = 0; //Счетчик итераций
        double prevXi; // Для хранения предыдущего значения

        do {
            prevXi = xi; // Сохраняем текущее значение
            double gradient = calculateDerivative(expression, x, xi); // Вычисляем градиент
            xi = xi - learningRate * gradient * (isMaximization ? -1 : 1); // Обновляем значение xi

            // Ограничиваем xi диапазоном
            if (xi < lower) xi = lower;
            if (xi > upper) xi = upper;

            iteration++; // Увеличиваем счетчик итераций
        } while (Math.abs(xi - prevXi) > tolerance && iteration < maxIterations); // Проверка условия выхода

        x.setArgumentValue(xi);// Устанавливаем найденное значение x
        double result = expression.calculate(); // Вычисляем результат функции в найденной точке

        resultView.setText("Результат: x = " + xi + ", f(x) = " + result + " Метод: Градиентыный спуск");
    }
    private void performFibonacciSearch(Expression expression, Argument x, double a, double b) {
        int n = 20; // Количество итераций
        double tolerance = 1e-6; // Точность

        double[] fib = new double[n+2]; // Массив для хранения чисел Фибоначчи
        fib[0] = 0;
        fib[1] = 1;
        for (int i = 2; i <= n+1; i++) {
            fib[i] = fib[i-1] + fib[i-2]; // Генерация чисел Фибоначчи
        }

        double c = a + (fib[n - 2] / fib[n]) * (b - a);
        double d = a + (fib[n - 1] / fib[n]) * (b - a);

        x.setArgumentValue(c);
        double fc = expression.calculate(); // Вычисление значения функции в c

        x.setArgumentValue(d);
        double fd = expression.calculate(); // Вычисление значения функции в d

        for (int i = n; i > 1; i--) {
            if ((fc < fd) ^ isMaximization) {
                b = d; // Если значение в c меньше, сужаем диапазон
                d = c; // Следующее значение d становится c
                fd = fc; // Значение функции в d обновляется
                c = a + (fib[i - 3] / fib[i - 1]) * (b - a); // Вычисление нового c
                x.setArgumentValue(c);
                fc = expression.calculate(); // Вычисление значения функции в новом c
            } else {
                a = c; // Если значение в d меньше или равно, сужаем диапазон
                c = d; // Следующее значение c становится d
                fc = fd; // Значение функции в c обновляется
                d = a + (fib[i - 2] / fib[i - 1]) * (b - a); // Вычисление нового d
                x.setArgumentValue(d);
                fd = expression.calculate(); // Вычисление значения функции в новом d
            }
        }

        double xi = (b + a) / 2; // Финальное значение x
        x.setArgumentValue(xi);
        double result = expression.calculate(); // Вычисление результата функции в xi

        resultView.setText("Результат: x = " + xi + ", f(x) = " + result + " Метод: Фибоначчи");
    }


    private double calculateDerivative(Expression expression, Argument x, double xi) {
        double delta = 1e-5;
        x.setArgumentValue(xi - delta);
        double f1 = expression.calculate();

        x.setArgumentValue(xi + delta);
        double f2 = expression.calculate();

        return (f2 - f1) / (2 * delta);
    }
    private boolean hasDerivative(Expression expression, Argument x, double lower, double upper) {
        try {
            double delta = 1e-5;
            for (double xi = lower; xi <= upper; xi += (upper - lower) / 10) {
                x.setArgumentValue(xi);
                double f1 = expression.calculate();

                x.setArgumentValue(xi + delta);
                double f2 = expression.calculate();

                double derivative = (f2 - f1) / delta;

                if (Double.isNaN(derivative) || Double.isInfinite(derivative)) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    private void performGoldenSectionSearch(Expression expression, Argument x, double a, double b) {
        double gr = (Math.sqrt(5) + 1) / 2; // Золотое сечение
        double tolerance = 1e-6;// Допустимая точность

        double c = b - (b - a) / gr; // Вычисление точки c
        double d = a + (b - a) / gr; // Вычисление точки d

        while (Math.abs(b - a) > tolerance) {  // Основной цикл
            x.setArgumentValue(c);
            double fc = expression.calculate(); // Значение функции в точке c

            x.setArgumentValue(d);
            double fd = expression.calculate(); // Значение функции в точке d

            if ((fc < fd) ^ isMaximization) { // Условие для обновления интервала
                b = d; // Убираем правую часть интервала
            } else {
                a = c; // Убираем левую часть интервала
            }

            c = b - (b - a) / gr;
            d = a + (b - a) / gr;
        }

        double xi = (b + a) / 2; // Находим среднее значение интервала
        x.setArgumentValue(xi); // Устанавливаем найденное значение x
        double result = expression.calculate(); // Вычисляем результат функции в найденной точке

        resultView.setText("Результат: x = " + xi + ", f(x) = " + result + " Метод: Золотое сечение");
    }
}

