package no.magott.nicolas.crawler;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.task.TaskExecutor;

public class MockUpCrawler {

	@Autowired
	private TaskExecutor taskExecutor;

	private List<String> urls = Arrays.asList("http://www.mysite.com",
			"http://www.yoursite.com");

	
	public void doCrawl() {
		for (String url : urls) {
			doCrawl(url);
		}

	}

	private FutureTask<Boolean> doCrawl(String url) {
		FutureTask<Boolean> crawlingTask = createFuture();
		taskExecutor.execute(crawlingTask);
		return crawlingTask;

	}

	private FutureTask<Boolean> createFuture() {
		final FutureTask<Boolean> task = new FutureTask<Boolean>(
				new Callable<Boolean>() {
					public Boolean call() throws Exception {
						// Your crawling magic goes here:
						System.out.println("Crawling something");
						return true;
					}
				});
		return task;
	}
	
	public void crawl(){
		ConfigurableApplicationContext context = null;
//		/magott-jz-txgotchas/src/test/java/no/magott/nicolas/crawler/crawler-context.xml
		context = new ClassPathXmlApplicationContext(
				"crawler-context.xml");
		context.getAutowireCapableBeanFactory().autowireBeanProperties(this,
				AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, false);
		
		doCrawl();
		context.close();
	}

	public static void main(String[] args) {
		MockUpCrawler crawler = new MockUpCrawler();
		crawler.crawl();
	}

}
