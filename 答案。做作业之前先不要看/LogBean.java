package Log;

import java.util.Date;

/*
 * ログBean定義
 */
public class LogBean {
	
	/**
	 * ログレベル
	 */
	private String Level;
	public String getLevel() {
		return Level;
	}
	public void setLevel(String level) {
		Level = level;
	}
	
	/**
	 * 生成時刻
	 */
	private Date timeStamp;
	public Date getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}
	
	/**
	 * 生成元機能
	 */
	private String producer;
	public String getProducer() {
		return producer;
	}
	public void setProducer(String producer) {
		this.producer = producer;
	}
	
	/**
	 * ログ内容
	 */
	private String content;
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
}
