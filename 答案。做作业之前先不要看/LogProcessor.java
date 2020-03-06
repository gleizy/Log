package Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class LogProcessor {

	//定数定義
	private static final String LEVEL_ERROR = "ERROR";
	private static final String LEVEL_FATAL = "FATAL";
	private static final String LEVEL_CRITICAL = "CRITICAL";
	private static final DateFormat JAVA_DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
	private static final DateFormat PYTHON_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
	private static final DateFormat OUTPUT_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS");
	
	/**
	 * Javaログの文字列からログBeanへ変換する
	 * @param log Javaログの文字列
	 * @return　変換したログBean
	 * @throws ParseException 日付変換失敗した場合、例外をスローする
	 */
	private static LogBean convertJavaLog(String log) throws ParseException {
		LogBean bean = new LogBean();
		
		//１つ目の'['から１つ目の']'まではログレベルとして、余計な半角スペースを取り除く
		bean.setLevel(
				log.substring(1, log.indexOf("]")).trim()
				);
		
		//１つ目の']'から最後の'['の間の文字列を生成時刻とする。
		bean.setTimeStamp(JAVA_DATE_FORMAT.parse(
				log.substring(log.indexOf("]") + 1, log.lastIndexOf("[")).trim()
				));
		
		//' - 'で文字列を分割する。
		String[] strArray = log.split(" - ");
		//分割した文字列の２つ目は生成元機能とする。
		bean.setProducer(strArray[1]);
		//分割した文字列の３つ目はログ内容とする。
		bean.setContent(strArray[2]);
		
		return bean;
	}
	
	/**
	 * Pythonログの文字列からログBeanへ変換する
	 * @param log Pythonログの文字列
	 * @return　変換したログBean
	 * @throws ParseException 日付変換失敗した場合、例外をスローする
	 */
	private static LogBean convertPythonLog(String log) throws ParseException {
		LogBean bean = new LogBean();
		
		//ログの先頭２３文字を生成時刻とする。
		bean.setTimeStamp(PYTHON_DATE_FORMAT.parse(
				log.substring(0, 23)
				));
		
		//２４文字以降の文字列を切り取る。
		log = log.substring(23);
		
		//':'で文字列を分割する。
		String[] strArray = log.split(":");
		//分割した文字列の１つ目は生成元機能とする。
		bean.setProducer(strArray[0]);
		
		//分割した文字列の２つ目を更に２つの半角スペース'  'で分割する。
		strArray = strArray[1].split("  ");
		//分割した文字列の１つ目はログレベルとして、「CRITICAL⇒FATAL」変換する。
		bean.setLevel(
				strArray[0].trim().replaceAll(LEVEL_CRITICAL, LEVEL_FATAL)
				);
		//分割した文字列の２つ目はログ内容とする。
		bean.setContent(strArray[1].trim());
		
		return bean;
	}
	
	
	public static void main(String[] args) {
		
		FileInputStream inStream = null;
		FileOutputStream outStream = null;

		try {
			//PointCalculator_20200211.logの内容を読み込む。
			List<String> javaErrorLogList = new ArrayList<>();
			inStream = new FileInputStream("C:/tmp/PointCalculator_20200211.log");
			InputStreamReader isr = new InputStreamReader(inStream);
			BufferedReader reader = new BufferedReader(isr);
			//一行を読む
			String line = reader.readLine();
			while(line != null) {
				if (line.indexOf(LEVEL_ERROR) >= 0 || line.indexOf(LEVEL_FATAL) >= 0) {
					javaErrorLogList.add(line);
				}
				line = reader.readLine();
			}
			inStream.close();
			
			//Python.logの内容を読み込む。
			List<String> pythonErrorLogList = new ArrayList<>();
			inStream = new FileInputStream("C:/tmp/Python.log");
			isr = new InputStreamReader(inStream);
			reader = new BufferedReader(isr);
			//一行を読む
			line = reader.readLine();
			while(line != null) {
				if (line.indexOf(LEVEL_ERROR) >= 0 || line.indexOf(LEVEL_CRITICAL) >= 0) {
					pythonErrorLogList.add(line);
				}
				line = reader.readLine();
			}
			
			//ログ内容を文字列からログBeanへ変換し、生成時刻で順番を並べる。
			List<LogBean> logBeanList = new ArrayList<>();
			//Javaログ処理
			for (String errorlog : javaErrorLogList) {
				//Javaログ内容を文字列からログBeanへ変換する。
				LogBean bean = convertJavaLog(errorlog);
				
				//出力のログBeanリストから全ての生成時刻をチェックして、挿入位置を探す。
				int insertIndex;
				for (insertIndex = 0; insertIndex < logBeanList.size(); insertIndex++) {
					if (logBeanList.get(insertIndex).getTimeStamp().compareTo(bean.getTimeStamp()) > 0) {
						//Beanリストの中に、現在のログの生成時刻より大きいログを見つかった。
						//変数insertIndexの値は、挿入位置となる。
						break;
					}
				}
				//見つかったログの位置に、現在のログを挿入する。
				logBeanList.add(insertIndex, bean);
			}			
			//Pythonログ処理
			for (String errorlog : pythonErrorLogList) {
				//Pythonログ内容を文字列からログBeanへ変換する。
				LogBean bean = convertPythonLog(errorlog);
				
				//出力のログBeanリストから全ての生成時刻をチェックして、挿入位置を探す。
				int insertIndex;
				for (insertIndex = 0; insertIndex < logBeanList.size(); insertIndex++) {
					if (logBeanList.get(insertIndex).getTimeStamp().compareTo(bean.getTimeStamp()) > 0) {
						//Beanリストの中に、現在のログの生成時刻より大きいログを見つかった。
						//変数insertIndexの値は、挿入位置となる。
						break;
					}
				}
				//見つかったログの位置に、現在のログを挿入する。
				logBeanList.add(insertIndex, bean);
			}
			
			//出力ファイルerror.logを開く。
			outStream = new FileOutputStream("C:/tmp/error.log");
			OutputStreamWriter osw = new OutputStreamWriter(outStream);
			
			//ログBeanリストの順番に出力する
			StringBuilder sb;
			for (LogBean bean : logBeanList) {
				sb = new StringBuilder();
				sb.append(bean.getLevel());
				sb.append(" - ");
				sb.append(OUTPUT_DATE_FORMAT.format(bean.getTimeStamp()));
				sb.append(" - ");
				sb.append(bean.getProducer());
				sb.append(" - ");
				sb.append(bean.getContent());
				sb.append(System.lineSeparator());
				
				osw.write(sb.toString());
			}
			
			
			//出力内容をフラッシュする。
			osw.flush();
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (inStream != null) {
				//InputStreamが既に開いた場合、クローズする。
				try {
					inStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (outStream != null) {
				//OutputStreamが既に開いた場合、クローズする。
				try {
					outStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}

}
