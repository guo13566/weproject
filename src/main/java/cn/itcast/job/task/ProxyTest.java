package cn.itcast.job.task;

import cn.itcast.job.selenium.SeleniumDownloader;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.downloader.HttpClientDownloader;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.proxy.Proxy;
import us.codecraft.webmagic.proxy.SimpleProxyProvider;
import us.codecraft.webmagic.selector.Html;

@Component
public class ProxyTest implements PageProcessor {

    private String url = "https://search.51job.com/list/000000,000000,0000,01%252c32,9,99," +
            "Java,2,1.html?lang=c&postchannel=0000&workyear=99&cotype=99&degreefrom=99" +
            "&jobterm=99&companysize=99&ord_field=0&dibiaoid=0&line=&welfare=";
//    @Scheduled(fixedDelay = 60000)
    public void process(){
        //创建下载器Downloader
        HttpClientDownloader httpClientDownloader = new HttpClientDownloader();
        //给下载器设置代理服务器信息
        httpClientDownloader.setProxyProvider(SimpleProxyProvider.from(new Proxy("113.100.209.91",3128)));
//        new SeleniumDownloader("D:\\chrome\\chromedriver.exe");
        Spider.create(new ProxyTest())
                .addUrl("http://ip.chinaz.com/getip")
//                .addUrl(url)
                .setDownloader(httpClientDownloader)    //设置代理服务器
                .run();
    }
    @Override
    public void process(Page page) {
        Html html = page.getHtml();
        System.out.println(html);
    }
    Site site = Site.me();
//private Site site = Site.me()
//        .setCharset("gbk")
//        .setTimeOut(30*1000)
//        .setRetrySleepTime(12000)
//        .setRetryTimes(3);
    @Override
    public Site getSite() {
        return site;
    }
}
