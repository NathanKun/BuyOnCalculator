/**
 * 代购计算器 MainActivity
 * @author 何俊阳
 * @version 3.0.5
 */
package com.nathankun.buyonsbspricescalculator;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * 首页
 * @author 何俊阳
 * @version 3.0.5
 */
public class MainActivity extends AppCompatActivity {
    /**
     * 连接雅虎api后返回的字符串
     */
    private static String resultStr = "";

    /**
     * 储存当前正在使用的币种
     */
    private static String currency = "EUR";

    /**
     * 颜文字点击次数
     */
    private static  int emojiCounter = 0;

    /**
     * 颜文字点击
     * 还是先不用了吧= =
     * @param view 按钮所需
     */
    public void emojiOnClick (View view){
        emojiCounter++;
        ImageView imageView_emoji = (ImageView)findViewById(R.id.imageView_emoji);
        if(emojiCounter == 5){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                imageView_emoji.setImageDrawable(getDrawable(R.mipmap.emoji2));
            } else {
                imageView_emoji.setImageDrawable(getResources().getDrawable(R.mipmap.emoji2));
            }
        }
        else if (emojiCounter == 15){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                imageView_emoji.setImageDrawable(getDrawable(R.mipmap.emoji3));
            } else {
                imageView_emoji.setImageDrawable(getResources().getDrawable(R.mipmap.emoji3));
            }
        }
        else if (emojiCounter == 30){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                imageView_emoji.setImageDrawable(getDrawable(R.mipmap.emoji4));
            } else {
                imageView_emoji.setImageDrawable(getResources().getDrawable(R.mipmap.emoji4));
            }
        }
    }

    /**
     * 检测editText的改动
     */
    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        /**
         * 一旦发生改变就计算结果
         * @param s s
         * @param start start
         * @param before before
         * @param count count
         */
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            calculateResult();
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    /**
     * 创建类时执行的方法
     *
     * @param savedInstanceState savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置显示的页面是 activity_main
        setContentView(R.layout.activity_main);
        //工具栏ActionBar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //悬浮按钮
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogSave();
            }
        });

        //读取退出前选择的文本，设置对应文本
        loadConfig();

        //获取币种的价格
        new VisitWebAsyncTask().execute(currency);

        //更新旧版本的save.txt文件
        updateSaveTxt();

        //给editText添加监听器
        EditText editText_currencyPrice = (EditText) findViewById(R.id.editText_currencyPrice);
        EditText editText_productPrice = (EditText) findViewById(R.id.editText_productPrice);
        EditText editText_discount = (EditText) findViewById(R.id.editText_discount);
        EditText editText_fee = (EditText) findViewById(R.id.editText_fee);
        EditText editText_shippingFee = (EditText) findViewById(R.id.editText_shippingFee);
        EditText editText_otherFee = (EditText) findViewById(R.id.editText_otherFee);
        editText_currencyPrice.addTextChangedListener(textWatcher);
        editText_productPrice.addTextChangedListener(textWatcher);
        editText_discount.addTextChangedListener(textWatcher);
        editText_fee.addTextChangedListener(textWatcher);
        editText_shippingFee.addTextChangedListener(textWatcher);
        editText_otherFee.addTextChangedListener(textWatcher);

        //颜文字点击计数器归零
        emojiCounter = 0;
    }

    /**
     * 工具栏菜单方法
     *
     * @param menu 菜单
     * @return boolean
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * action bar选项被选的方法
     *
     * @param item 被选中的项目
     * @return boolean
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //转到About的Activity
        if (id == R.id.action_about) {
            startActivity(new Intent(MainActivity.this, AboutActivity.class));
            return true;
        }
        //显示历史记录对话框
        if (id == R.id.action_history) {
            dialogLoad();
            return true;
        }
        //显示币种对话框
        if (id == R.id.action_currency) {
            dialogCurrency();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * 获得的字符串存到 resultStr 中
     *
     * @param view button所需
     * @throws MalformedURLException
     */
    public void getPrice(View view) throws MalformedURLException {
        //联网获取汇率的线程
        Thread visitYahooAPIThread = new Thread(new VisitWebRunnable());
        visitYahooAPIThread.start();
        TextView textView = new TextView(this);
        textView.setTextSize(15);
        try {
            visitYahooAPIThread.join();
            if (!resultStr.equals("")) {
                textView.setText(resultStr);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        EditText editText_currencyPrice = (EditText) findViewById(R.id.editText_currencyPrice);
        editText_currencyPrice.setText(analyseResultStr());
    }

    /**
     * 获取指定URL的响应字符串
     *
     * @param urlString 指定的URL
     * @return 获得的字符串
     */
    private String getURLResponse(String urlString) {
        HttpURLConnection conn = null; //连接对象
        InputStream is = null;
        String resultData = "";
        try {
            URL url = new URL(urlString); //URL对象
            conn = (HttpURLConnection) url.openConnection(); //使用URL打开一个链接
            conn.setDoInput(true); //允许输入流，即允许下载
            conn.setDoOutput(true); //允许输出流，即允许上传
            conn.setUseCaches(false); //不使用缓冲
            conn.setRequestMethod("GET"); //使用get请求
            is = conn.getInputStream();   //获取输入流，此时才真正建立链接
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader bufferReader = new BufferedReader(isr);
            String inputLine;
            while ((inputLine = bufferReader.readLine()) != null) {
                resultData += inputLine + "\n";
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                conn.disconnect();
            }
        }

        return resultData;
    }

    /**
     * 将读取了的数据显示出来
     *
     * @param which 选择的项目的序号
     */
    private void showRead(int which) {

        String[] list = readFile();
        String[] splited = new String[0];
        if (list != null) {
            splited = list[which].split(";");
        }

        EditText editText_currencyPrice = (EditText) findViewById(R.id.editText_currencyPrice);
        EditText editText_productPrice = (EditText) findViewById(R.id.editText_productPrice);
        EditText editText_discount = (EditText) findViewById(R.id.editText_discount);
        EditText editText_fee = (EditText) findViewById(R.id.editText_fee);
        EditText editText_shippingFee = (EditText) findViewById(R.id.editText_shippingFee);
        EditText editText_otherFee = (EditText) findViewById(R.id.editText_otherFee);
        EditText editText_resultPrice = (EditText) findViewById(R.id.editText_resultPrice);

        editText_currencyPrice.setText(splited[1]);
        editText_productPrice.setText(splited[2]);
        editText_discount.setText(splited[3]);
        editText_fee.setText(splited[4]);
        editText_shippingFee.setText(splited[5]);
        editText_otherFee.setText(splited[6]);
        editText_resultPrice.setText(splited[7]);
        currency = splited[8];
        setTextCurrency();
    }

    /**
     * 删除一个记录
     *
     * @param which 选择的项目的序号
     */
    private void deleteItem(int which) {
        String[] list = readFile();
        String[] newList = new String[0];
        if (list != null) {
            newList = new String[list.length - 1];
            System.arraycopy(list, 0, newList, 0, which);
            System.arraycopy(list, which + 1, newList, which, list.length - (which + 1));
        }
        String saveStr = "";
        for (int i = 0; i < newList.length; i++) {
            saveStr = saveStr + newList[i];
            if (i != newList.length - 1) {
                saveStr = saveStr + "\n";
            }
        }
        //保存
        String filename = "save.txt";
        FileOutputStream outputStream;
        //写入文件
        try {
            outputStream = openFileOutput(filename, MainActivity.MODE_PRIVATE);
            outputStream.write(saveStr.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 分析接收到的字符串
     * 如果是正常的 "CNY",X.xxxx 则返回汇率
     * 否则返回 failed
     *
     * @return 欧元汇率对应的字符串或failed
     */
    private String analyseResultStr() {
        String[] strSplited = resultStr.split(",");
        if (strSplited[0].equals("\"CNY\"") || strSplited[0].equals("\"USD\"") || strSplited[0].equals("\"GBP\"") ||
                strSplited[0].equals("\"CAD\"") || strSplited[0].equals("\"AUD\"") || strSplited[0].equals("\"HKD\"") ||
                strSplited[0].equals("\"JPY\"") || strSplited[0].equals("\"KRW\"")) {
            return resultStr.substring(6, 12);
        }
        else return "failed";
    }

    /**
     * 获取以输入的数据，转化为浮点，计算出商品售价，转成字符串，放到EditText中
     *
     * @param view button所需
     */
    public void calculateResult(View view) {
        EditText editText_currencyPrice = (EditText) findViewById(R.id.editText_currencyPrice);
        EditText editText_productPrice = (EditText) findViewById(R.id.editText_productPrice);
        EditText editText_discount = (EditText) findViewById(R.id.editText_discount);
        EditText editText_fee = (EditText) findViewById(R.id.editText_fee);
        EditText editText_shippingFee = (EditText) findViewById(R.id.editText_shippingFee);
        EditText editText_otherFee = (EditText) findViewById(R.id.editText_otherFee);
        EditText editText_resultPrice = (EditText) findViewById(R.id.editText_resultPrice);

        if (editText_currencyPrice.getText().toString().equals("failed")) {
            editText_resultPrice.setText("汇率错误");
            return;
        }
        String euroPrice = editText_currencyPrice.getText().toString();
        String producePrice = editText_productPrice.getText().toString();
        String discount = editText_discount.getText().toString();
        String fee = editText_fee.getText().toString();
        String shippingFee = editText_shippingFee.getText().toString();
        String otherFee = editText_otherFee.getText().toString();
        String resultPrice;

        float euroPrice_float;
        float producePrice_float;
        float discount_float;
        float fee_float;
        float shippingFee_float;
        float otherFee_float;
        float resultPrice_float;
        if (!euroPrice.equals("")) {
            euroPrice_float = Float.parseFloat(euroPrice);
        } else euroPrice_float = 0.0f;
        if (!producePrice.equals("")) {
            producePrice_float = Float.parseFloat(producePrice);
        } else producePrice_float = 0.0f;
        if (!discount.equals("")) {
            discount_float = Float.parseFloat(discount);
        } else discount_float = 100f;
        if (!fee.equals("")) {
            fee_float = Float.parseFloat(fee);
        } else fee_float = 0f;
        if (!shippingFee.equals("")) {
            shippingFee_float = Float.parseFloat(shippingFee);
        } else shippingFee_float = 0;
        if (!otherFee.equals("")) {
            otherFee_float = Float.parseFloat(otherFee);
        } else otherFee_float = 0;

        float producePriceDiscounted = producePrice_float * discount_float / 100;
        float feeCalculated = producePrice_float * fee_float / 100;
        resultPrice_float = (producePriceDiscounted + feeCalculated + shippingFee_float + otherFee_float)
                * euroPrice_float;

        resultPrice = String.valueOf(resultPrice_float);
        editText_resultPrice.setText(resultPrice);
    }


    /**
     * 获取以输入的数据，转化为浮点，计算出商品售价，转成字符串，放到EditText中
     */
    public void calculateResult() {
        EditText editText_currencyPrice = (EditText) findViewById(R.id.editText_currencyPrice);
        EditText editText_productPrice = (EditText) findViewById(R.id.editText_productPrice);
        EditText editText_discount = (EditText) findViewById(R.id.editText_discount);
        EditText editText_fee = (EditText) findViewById(R.id.editText_fee);
        EditText editText_shippingFee = (EditText) findViewById(R.id.editText_shippingFee);
        EditText editText_otherFee = (EditText) findViewById(R.id.editText_otherFee);
        EditText editText_resultPrice = (EditText) findViewById(R.id.editText_resultPrice);

        if (editText_currencyPrice.getText().toString().equals("failed")) {
            editText_resultPrice.setText("汇率错误");
            return;
        }
        String euroPrice = editText_currencyPrice.getText().toString();
        String producePrice = editText_productPrice.getText().toString();
        String discount = editText_discount.getText().toString();
        String fee = editText_fee.getText().toString();
        String shippingFee = editText_shippingFee.getText().toString();
        String otherFee = editText_otherFee.getText().toString();
        String resultPrice;

        float euroPrice_float;
        float producePrice_float;
        float discount_float;
        float fee_float;
        float shippingFee_float;
        float otherFee_float;
        float resultPrice_float;
        if (!euroPrice.equals("")&&!euroPrice.equals(".")) {
            euroPrice_float = Float.parseFloat(euroPrice);
        } else euroPrice_float = 0.0f;
        if (!producePrice.equals("")&&!producePrice.equals(".")) {
            producePrice_float = Float.parseFloat(producePrice);
        } else producePrice_float = 0.0f;
        if (!discount.equals("")&&!discount.equals(".")) {
            discount_float = Float.parseFloat(discount);
        } else discount_float = 100f;
        if (!fee.equals("")&&!fee.equals(".")) {
            fee_float = Float.parseFloat(fee);
        } else fee_float = 0f;
        if (!shippingFee.equals("")&&!shippingFee.equals(".")) {
            shippingFee_float = Float.parseFloat(shippingFee);
        } else shippingFee_float = 0;
        if (!otherFee.equals("")&&!otherFee.equals(".")) {
            otherFee_float = Float.parseFloat(otherFee);
        } else otherFee_float = 0;

        float producePriceDiscounted = producePrice_float * discount_float / 100;
        float feeCalculated = producePrice_float * fee_float / 100;
        resultPrice_float = (producePriceDiscounted + feeCalculated + shippingFee_float + otherFee_float)
                * euroPrice_float;

        resultPrice = String.valueOf(resultPrice_float);
        editText_resultPrice.setText(resultPrice);
    }

    /**
     * 对话框
     * 读取文件中保存的记录
     * ActionBar - 记录 调用
     */
    private void dialogLoad() {
        //读取文件到list
        String[] list = readFile();
        //空文件情况
        if (list == null) {
            Toast.makeText(MainActivity.this,
                    "啥都木有~", Toast.LENGTH_SHORT).show();
            return;
        }
        //得到对话框中要显示的内容
        String[] forshow = new String[list.length];
        for (int i = 0; i < list.length; i++) {
            String[] splited = list[i].split(";");
            forshow[i] = splited[0] + ": " + splited[7] + "CNY";
        }

        //自定义了的listner
        final ChoiceOnClickListener choiceListener = new ChoiceOnClickListener();
        //对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setIcon(android.R.drawable.ic_input_get);
        builder.setSingleChoiceItems(forshow, 0, choiceListener);
        DialogInterface.OnClickListener loadListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        showRead(choiceListener.getWhich());
                    }
                };
        DialogInterface.OnClickListener deleteListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        deleteItem(choiceListener.getWhich());
                        dialogLoad();
                    }
                };
        builder.setTitle("记录：");
        builder.setPositiveButton("读取", loadListener);
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setNeutralButton("删除", deleteListener);

        builder.create().show();
    }


    /**
     * 对话框
     * ActionBar - 币种 调用
     */
    private void dialogCurrency() {

        //得到对话框中要显示的内容
        String[] forshow = {"EUR", "USD", "GBP", "CAD", "AUD", "HKD", "JPY", "KRW"};


        //自定义了的listner
        final ChoiceOnClickListener choiceListener = new ChoiceOnClickListener();
        //对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setIcon(android.R.drawable.ic_input_get);
        builder.setSingleChoiceItems(forshow, 0, choiceListener);
        DialogInterface.OnClickListener loadListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        setCurrency(choiceListener.getWhich());
                        setTextCurrency();
                        //getPrice();
                        new VisitWebAsyncTask().execute(currency);

                    }
                };
        builder.setTitle("币种：");
        builder.setPositiveButton("选择", loadListener);
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }


    /**
     * 对话框
     * 返回键，确认退出
     */
    private void dialogExit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("确认退出吗？");

        builder.setTitle("提示");

        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                saveConfig();
                MainActivity.this.finish();
            }
        });

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

     builder.create().show();
     }

     /**
     * 对话框
     * 悬浮按钮-保存
     */
    private void dialogSave() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("请输入商品名字");
        builder.setIcon(android.R.drawable.ic_dialog_info);
        final EditText editText = new EditText(this);
        builder.setView(editText);
        builder.setPositiveButton("保存", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                saveFile(editText);
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    /**
     * 文件处理
     * 保存文件
     * 悬浮按钮调用
     *
     * @param editText 点保存后弹出对话框中的EditText
     */
    private void saveFile(EditText editText) {

        String saveStr;
        EditText editText_currencyPrice = (EditText) findViewById(R.id.editText_currencyPrice);
        EditText editText_productPrice = (EditText) findViewById(R.id.editText_productPrice);
        EditText editText_discount = (EditText) findViewById(R.id.editText_discount);
        EditText editText_fee = (EditText) findViewById(R.id.editText_fee);
        EditText editText_shippingFee = (EditText) findViewById(R.id.editText_shippingFee);
        EditText editText_otherFee = (EditText) findViewById(R.id.editText_otherFee);
        EditText editText_resultPrice = (EditText) findViewById(R.id.editText_resultPrice);

        String euroPrice = editText_currencyPrice.getText().toString();
        String producePrice = editText_productPrice.getText().toString();
        String discount = editText_discount.getText().toString();
        String fee = editText_fee.getText().toString();
        String shippingFee = editText_shippingFee.getText().toString();
        String otherFee = editText_otherFee.getText().toString();
        String resultPrice = editText_resultPrice.getText().toString();
        String entredText = editText.getText().toString();
        //处理掉文本框中输入了的回车
        if (entredText.contains("\n")) {
            String sp[] = entredText.split("\n");
            entredText = "";
            for (String aSp : sp) {
                entredText = entredText + aSp;
            }
        }

        //合成保存的字符串
        saveStr = entredText + ";" + euroPrice + ";" + producePrice + ";" + discount + ";" + fee + ";" + shippingFee + ";" + otherFee + ";" + resultPrice + ";" + currency;

        //保存
        String filename = "save.txt";
        String readStr = "";
        FileOutputStream outputStream;
        File file = new File(getFilesDir() + "/save.txt");
        //检查文件是否存在，不存在则创建
        if (!file.exists()) {
            new File(getFilesDir(), filename);
        }
        //读取文件，合并新旧内容
        InputStream inputStream;
        try {
            inputStream = new FileInputStream(getFilesDir() + "/save.txt");
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder buffer = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                buffer.append(line);
                buffer.append("\n");
            }
            readStr = buffer.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //写入文件
        try {
            outputStream = openFileOutput(filename, MainActivity.MODE_PRIVATE);
            outputStream.write(readStr.getBytes());
            outputStream.write(saveStr.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 文件处理
     * 读取文件
     * ActionBar调用
     */
    private String[] readFile() {

        String filename = "save.txt";
        String readStr;
        File file = new File(getFilesDir() + "/save.txt");
        //检查文件是否存在，不存在则创建
        if (!file.exists()) {
            new File(getFilesDir(), filename);
        }

        InputStream inputStream;
        try {
            inputStream = new FileInputStream(getFilesDir() + "/save.txt");
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder buffer = new StringBuilder();
            String line;
            ArrayList<String> readList = new ArrayList<>();
            while ((line = in.readLine()) != null) {
                buffer.append(line);
                buffer.append("\n");
                readList.add(line);
            }
            String[] readStrTab = new String[readList.size()];
            for (int i = 0; i < readList.size(); i++) {
                readStrTab[i] = readList.get(i);
            }
            readStr = buffer.toString();
            inputStream.close();
            in.close();
            if (readStr.length() <= 2) {
                return null;
            }
            return readStrTab;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 三大金刚按键事件
     *
     * @param keyCode keyCode
     * @param event   按键事件
     * @return boolean
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            dialogExit();
            return true;
        }
        return false;

    }

    /**
     * 查询目前所选的币种，返回币种对应的汇率获取连接
     * @param currency 目前所选的币种
     * @return 币种对应的汇率获取连接
     */
    public String getUrl(String currency) {
        if (currency.equals("EUR"))
            return "http://download.finance.yahoo.com/d/quotes.csv?e=.csv&f=c4l1&s=EURCNY=X";
        if (currency.equals("USD"))
            return "http://download.finance.yahoo.com/d/quotes.csv?e=.csv&f=c4l1&s=USDCNY=X";
        if (currency.equals("GBP"))
            return "http://download.finance.yahoo.com/d/quotes.csv?e=.csv&f=c4l1&s=GBPCNY=X";
        if (currency.equals("CAD"))
            return "http://download.finance.yahoo.com/d/quotes.csv?e=.csv&f=c4l1&s=CADCNY=X";
        if (currency.equals("AUD"))
            return "http://download.finance.yahoo.com/d/quotes.csv?e=.csv&f=c4l1&s=AUDCNY=X";
        if (currency.equals("HKD"))
            return "http://download.finance.yahoo.com/d/quotes.csv?e=.csv&f=c4l1&s=HKDCNY=X";
        if (currency.equals("JPY"))
            return "http://download.finance.yahoo.com/d/quotes.csv?e=.csv&f=c4l1&s=JPYCNY=X";
        if (currency.equals("KRW"))
            return "http://download.finance.yahoo.com/d/quotes.csv?e=.csv&f=c4l1&s=KRWCNY=X";

        return "";
    }

    /**
     * 根据对话框中的选择将currency变量设置成对应的币种
     * 最后调用saveConfig()保存
     * @param which 对话框中的选择
     */
    public void setCurrency(int which) {
        if (which == 0)
            currency = "EUR";
        if (which == 1)
            currency = "USD";
        if (which == 2)
            currency = "GBP";
        if (which == 3)
            currency = "CAD";
        if (which == 4)
            currency = "AUD";
        if (which == 5)
            currency = "HKD";
        if (which == 6)
            currency = "JPY";
        if (which == 7)
            currency = "KRW";

        saveConfig();
    }

    /**
     * 查询目前所选的币种，将页面的文本设置成与币种所对应
     */
    public void setTextCurrency() {
        TextView textView_euroPrice_result = (TextView) findViewById(R.id.textView_currencyPrice);
        TextView textView_productPrice = (TextView) findViewById(R.id.textView_productPrice);
        TextView textView_shippingFee = (TextView) findViewById(R.id.textView_shippingFee);
        TextView textView_otherFee = (TextView) findViewById(R.id.textView_otherFee);

        switch (currency) {
            case "EUR":
                textView_euroPrice_result.setText(String.format(getResources().getString(R.string.currencyPriceTextView), "EUR"));
                textView_productPrice.setText(String.format(getResources().getString(R.string.productPrice), "EUR"));
                textView_shippingFee.setText(String.format(getResources().getString(R.string.shippingFee), "EUR"));
                textView_otherFee.setText(String.format(getResources().getString(R.string.otherFee), "EUR"));
                break;
            case "USD":
                textView_euroPrice_result.setText(String.format(getResources().getString(R.string.currencyPriceTextView), "USD"));
                textView_productPrice.setText(String.format(getResources().getString(R.string.productPrice), "USD"));
                textView_shippingFee.setText(String.format(getResources().getString(R.string.shippingFee), "USD"));
                textView_otherFee.setText(String.format(getResources().getString(R.string.otherFee), "USD"));
                break;
            case "GBP":
                textView_euroPrice_result.setText(String.format(getResources().getString(R.string.currencyPriceTextView), "GBP"));
                textView_productPrice.setText(String.format(getResources().getString(R.string.productPrice), "GBP"));
                textView_shippingFee.setText(String.format(getResources().getString(R.string.shippingFee), "GBP"));
                textView_otherFee.setText(String.format(getResources().getString(R.string.otherFee), "GBP"));
                break;
            case "CAD":
                textView_euroPrice_result.setText(String.format(getResources().getString(R.string.currencyPriceTextView), "CAD"));
                textView_productPrice.setText(String.format(getResources().getString(R.string.productPrice), "CAD"));
                textView_shippingFee.setText(String.format(getResources().getString(R.string.shippingFee), "CAD"));
                textView_otherFee.setText(String.format(getResources().getString(R.string.otherFee), "CAD"));
                break;
            case "AUD":
                textView_euroPrice_result.setText(String.format(getResources().getString(R.string.currencyPriceTextView), "AUD"));
                textView_productPrice.setText(String.format(getResources().getString(R.string.productPrice), "AUD"));
                textView_shippingFee.setText(String.format(getResources().getString(R.string.shippingFee), "AUD"));
                textView_otherFee.setText(String.format(getResources().getString(R.string.otherFee), "AUD"));
                break;
            case "HKD":
                textView_euroPrice_result.setText(String.format(getResources().getString(R.string.currencyPriceTextView), "HKD"));
                textView_productPrice.setText(String.format(getResources().getString(R.string.productPrice), "HKD"));
                textView_shippingFee.setText(String.format(getResources().getString(R.string.shippingFee), "HKD"));
                textView_otherFee.setText(String.format(getResources().getString(R.string.otherFee), "HKD"));
                break;
            case "JPY":
                textView_euroPrice_result.setText(String.format(getResources().getString(R.string.currencyPriceTextView), "JPY"));
                textView_productPrice.setText(String.format(getResources().getString(R.string.productPrice), "JPY"));
                textView_shippingFee.setText(String.format(getResources().getString(R.string.shippingFee), "JPY"));
                textView_otherFee.setText(String.format(getResources().getString(R.string.otherFee), "JPY"));
                break;
            case "KRW":
                textView_euroPrice_result.setText(String.format(getResources().getString(R.string.currencyPriceTextView), "KRW"));
                textView_productPrice.setText(String.format(getResources().getString(R.string.productPrice), "KRW"));
                textView_shippingFee.setText(String.format(getResources().getString(R.string.shippingFee), "KRW"));
                textView_otherFee.setText(String.format(getResources().getString(R.string.otherFee), "KRW"));
                break;
        }

    }

    /**
     * 升级3.0版本后需要修改原来保存的文件来兼容多币种
     */
    public void updateSaveTxt() {
        String[] list = readFile();
        if (list != null) {
            String[] aStr = list[0].split(";");
            if (aStr.length == 8) {
                for (int i = 0; i < list.length; i++) {
                    list[i] = list[i] + ";EUR";
                }
                String saveStr = "";

                for (String str : list) {
                    saveStr = saveStr + str + "\n";
                }
                //保存
                String filename = "save.txt";
                FileOutputStream outputStream;
                //File file = new File(getFilesDir() + "/save.txt");
                //写入文件
                try {
                    outputStream = openFileOutput(filename, MainActivity.MODE_PRIVATE);
                    outputStream.write(saveStr.getBytes());
                    outputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }


    }

    /**
     * 选择币种时执行该方法，用于保存当前选择的币种
     */
    public void saveConfig(){
        String filename = "config.txt";
        FileOutputStream outputStream;
        File file = new File(getFilesDir() + "/" + filename);
        //检查文件是否存在，不存在则创建
        if (!file.exists()) {
            new File(getFilesDir(), filename);
        }
        //写入文件
        try {
            outputStream = openFileOutput(filename, MainActivity.MODE_PRIVATE);
            outputStream.write(currency.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 打开应用时执行该方法，用于读取关闭前选择的币种
     * 若第一次打开则为欧元
     */
    public void loadConfig() {
        String filename = "config.txt";
        FileInputStream inputStream;
        File file = new File(getFilesDir() + "/" + filename);
        if (file.exists()) {
            try {
                inputStream = new FileInputStream(getFilesDir() + "/" + filename);
                BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                line = in.readLine();
                currency = line;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        setTextCurrency();
    }

    /**
     * 线程类
     * 3.0后弃用，改用AsyncTask
     */
    private class VisitWebRunnable implements Runnable {

        /**
         * 利用getURLResponse(URL url)方法
         * 从雅虎API获取字符串，存到resultStr中
         */
        @Override
        public void run() {
            String url = getUrl(currency);
            resultStr = getURLResponse(url);
        }
    }

    /**
     * 后台线程，用于后台联网获取汇率
     */
    public class VisitWebAsyncTask extends AsyncTask<String, Void, String> {
        /**
         * 线程结束后执行
         * 将结果存入resultStr
         * 调用分析方法并将分析结果放入汇率editText
         * @param result 线程执行的结果
         */
        @Override
        protected void onPostExecute(String result) {
            resultStr = result;
            EditText editText_currencyPrice = (EditText) findViewById(R.id.editText_currencyPrice);
            editText_currencyPrice.setText(analyseResultStr());
        }

        /**
         * 后台执行的内容
         * 联网获取汇率
         * @param c 线程输入的变量，这里是币种对应的汇率获取url
         * @return URL的内容
         */
        @Override
        protected String doInBackground(String... c) {
            String url = getUrl(c[0]);
            return  getURLResponse(url);
        }
    }

    /**
     * 单选对话框，检测选了哪项
     */
    private class ChoiceOnClickListener implements DialogInterface.OnClickListener {

        private int which = 0;

        @Override
        public void onClick(DialogInterface dialogInterface, int which) {
            this.which = which;
        }

        /**
         * witchGetter
         *
         * @return 选择的项目序号
         */
        public int getWhich() {
            return which;
        }
    }
}
