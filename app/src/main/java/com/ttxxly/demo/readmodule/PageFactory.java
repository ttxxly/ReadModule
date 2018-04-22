package com.ttxxly.demo.readmodule;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.content.ContextCompat;
import android.widget.ProgressBar;

import com.ttxxly.demo.readmodule.utils.ScreenUtils;
import com.ttxxly.demo.readmodule.utils.StringUtils;

import java.io.UnsupportedEncodingException;
import java.nio.MappedByteBuffer;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

/**
 * description:
 *
 * @author Administrator
 * @date 2018-04-22
 * mail ttxxly@gmail.com
 */
public class PageFactory {
    private Context mContext;
    /**
     * 屏幕宽高
     */
    private int mHeight, mWidth;
    /**
     * 文字区域宽高
     */
    private int mVisibleHeight, mVisibleWidth;
    /**
     * 间距
     */
    private int marginHeight, marginWidth;
    /**
     * 字体大小
     */
    private int mFontSize, mTitleFontSize;
    /**
     * 每页行数
     */
    private int mPageLineCount;
    /**
     * 行间距
     **/
    private int mLineSpace;
    /**
     * 章节总长度
     */
    private int mbBufferLen;
    /**
     * 保存该章节的字符串
     */
    private String text;
    /**
     * 页首页尾的位置
     */
    private int curEndPos = 0, curBeginPos = 0, tempBeginPos, tempEndPos;
    private int currentChapter, tempChapter;
    /**
     * 将文本文件分行保存， Vector是一个动态数组
     */
    private Vector<String> mLines = new Vector<>();

    private Paint mPaint;
    private Paint mTitlePaint;
    private Bitmap mBookPageBg;

    private DecimalFormat decimalFormat = new DecimalFormat("#0.00");
    private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
    private int timeLen = 0, percentLen = 0;
    private String time;
    private int battery = 40;
    private Rect rectF;
    private ProgressBar batteryView;
    private Bitmap batteryBitmap;

    private String bookId;
    private int chapterSize = 0;
    private int currentPage = 1;
    private String charset = "UTF-8";


   PageFactory(Context context, int width, int height, int fontSize) {
        mContext = context;
        mWidth = ScreenUtils.getScreenWidth(context);
        mHeight = ScreenUtils.getScreenHeight(context);
        mFontSize = fontSize;
        mLineSpace = mFontSize / 5 * 2;
        mTitleFontSize = ScreenUtils.dpToPxInt(context, 16);
        marginWidth = ScreenUtils.dpToPxInt(context, 15);
        marginHeight = ScreenUtils.dpToPxInt(context, 15);
        mVisibleHeight = mHeight - marginHeight * 2 - mTitleFontSize * 2 - mLineSpace * 2;
        mVisibleWidth = mWidth - marginWidth * 2;
        mPageLineCount = mVisibleHeight / (mFontSize + mLineSpace);
        rectF = new Rect(0, 0, mWidth, mHeight);
        mbBufferLen = book.getContent().length();
        text = book.getContent();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setTextSize(mFontSize);
        mPaint.setColor(Color.BLACK);
        mTitlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTitlePaint.setTextSize(mTitleFontSize);
        mTitlePaint.setColor(Color.BLACK);
        timeLen = (int) mTitlePaint.measureText("00:00");
        percentLen = (int) mTitlePaint.measureText("00.00%");

        time = dateFormat.format(new Date());
    }

    /**
     * 绘制阅读页面
     *
     * @param canvas
     */
    public synchronized void onDraw(Canvas canvas) {
        if (mLines.size() == 0) {
            curEndPos = curBeginPos;
            mLines = pageDown();
        }
        if (mLines.size() > 0) {
            int y = marginHeight + (mLineSpace << 1);
            // 绘制背景
            if (mBookPageBg != null) {
                canvas.drawBitmap(mBookPageBg, null, rectF, null);
            } else {
                canvas.drawColor(Color.WHITE);
            }
            // 绘制标题
            canvas.drawText(book.getTitle(), marginWidth, y, mTitlePaint);
            y += mLineSpace + mTitleFontSize;
            // 绘制阅读页面文字
            for (String line : mLines) {
                y += mLineSpace;
                if (line.endsWith("@")) {
                    canvas.drawText(line.substring(0, line.length() - 1), marginWidth, y, mPaint);
                    y += mLineSpace;
                } else {
                    canvas.drawText(line, marginWidth, y, mPaint);
                }
                y += mFontSize;
            }

            float percent = (float) currentChapter * 100 / chapterSize;
            canvas.drawText(decimalFormat.format(percent) + "%", (mWidth - percentLen) / 2,
                    mHeight - marginHeight, mTitlePaint);

            String mTime = dateFormat.format(new Date());
            canvas.drawText(mTime, mWidth - marginWidth - timeLen, mHeight - marginHeight, mTitlePaint);
        }
    }

    private Vector<String> pageDown() {
        Vector<String> lines = new Vector<>();
        String strParagraph = book.getContent();
        int paraSpace = 0;
        mPageLineCount = mVisibleHeight / (mFontSize + mLineSpace);
        while ((lines.size() < mPageLineCount) && (curEndPos < mbBufferLen)) {
            byte[] parabuffer = readParagraphForward(curEndPos);
            curEndPos += parabuffer.length;
            try {
                strParagraph = new String(parabuffer, charset);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            // 段落中的换行符去掉，绘制的时候再换行
            strParagraph = strParagraph.replaceAll("\r\n", "  ")
                    .replaceAll("\n", " ");

            while (strParagraph.length() > 0) {
                int paintSize = mPaint.breakText(strParagraph, true, mVisibleWidth, null);
                lines.add(strParagraph.substring(0, paintSize));
                strParagraph = strParagraph.substring(paintSize);
                if (lines.size() >= mPageLineCount) {
                    break;
                }
            }
            lines.set(lines.size() - 1, lines.get(lines.size() - 1) + "@");
            if (strParagraph.length() != 0) {
                try {
                    curEndPos -= (strParagraph).getBytes(charset).length;
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            paraSpace += mLineSpace;
            mPageLineCount = (mVisibleHeight - paraSpace) / (mFontSize + mLineSpace);
        }
        return lines;
    }

    /**
     * 读取下一段落
     *
     * @param curEndPos 当前页结束位置指针
     * @return
     */
    private byte[] readParagraphForward(int curEndPos) {
        char b0;
        int i = curEndPos;
        while (i < mbBufferLen) {
            b0 = text.charAt(i++);
            if (b0 == 0x0a) {
                break;
            }
        }
        int nParaSize = i - curEndPos;
        char[] s = new char[nParaSize];
        for (i = 0; i < nParaSize; i++) {
            s[i] = text.charAt(curEndPos + i);
        }
        return StringUtils.getBytes(s);
    }

    /**
     * 读取上一段落
     *
     * @param curBeginPos 当前页起始位置指针
     * @return
     */
    private byte[] readParagraphBack(int curBeginPos) {
        char b0;
        int i = curBeginPos - 1;
        while (i > 0) {
            b0 = text.charAt(i++);
            if (b0 == 0x0a && i != curBeginPos - 1) {
                i++;
                break;
            }
            i--;
        }
        int nParaSize = curBeginPos - i;
        char[] s = new char[nParaSize];
        for (int j = 0; j < nParaSize; j++) {
            s[j] = text.charAt(i + j);
        }
        return StringUtils.getBytes(s);
    }
}
