package com.itheima.hbaseandes.utils;

import com.alibaba.fastjson.JSON;
import com.itheima.hbaseandes.bean.Article;
import org.apache.avro.data.Json;
import org.apache.hadoop.yarn.webapp.hamlet.Hamlet;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * @program: etl
 * @description: 1.获取客户端
 * 2.插入数据
 * 参数:List<Article>
 * 3.查询数据  词条 关键字查询
 * 参数:index type 关键字
 * 返回值:List<Article>
 * @author: Mr.Jiang
 * @create: 2019-08-18 20:25
 **/
public class EsUtil {

    public static TransportClient getClient() {

        TransportClient transportClient = null;
        try {
            Settings myes = Settings.builder().put("cluster.name", "myes").build();
            //获取客户端
            transportClient = new PreBuiltTransportClient(myes)
                    .addTransportAddress(new TransportAddress(InetAddress.getByName("node01"), 9300))
                    .addTransportAddress(new TransportAddress(InetAddress.getByName("node02"), 9300))
                    .addTransportAddress(new TransportAddress(InetAddress.getByName("node03"), 9300));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return transportClient;
    }

    /**
     * 2.批量插入数据
     * 参数:List<Article>
     *
     * @param list
     */
    public static void putData(List<Article> list) {

        //获取连接
        TransportClient client = getClient();
        BulkRequestBuilder bulkRequestBuilder = client.prepareBulk();

        for (Article article : list) {
            IndexRequestBuilder indexRequestBuilder = client.prepareIndex("articles", "article", article.getId())
                    //需要设置 XContentType.JSON
                    .setSource(JSON.toJSONString(article), XContentType.JSON);
            bulkRequestBuilder.add(indexRequestBuilder);
        }

        //插入数据
        bulkRequestBuilder.get();

        //关流
        client.close();
    }

    /**
     * * 3.查询数据  词条 关键字查询
     * * 参数:index type 关键字
     * * 返回值:
     *
     * @param keyWord
     * @return
     */
    public static List<Article> queryByKeyWord(String keyWord) {

        List<Article> articles = new ArrayList<>();
        //创建连接
        TransportClient client = getClient();

        //查询
        SearchResponse searchResponse = client.prepareSearch("articles")
                .setTypes("article")
                .setQuery(QueryBuilders.termQuery("title", keyWord))
                .get();

        SearchHit[] hits = searchResponse.getHits().getHits();
        for (SearchHit hit : hits) {

            String sourceAsString = hit.getSourceAsString();
            //将字符串转化为bean对象
            Article article = JSON.parseObject(sourceAsString, Article.class);
            articles.add(article);
        }
        return articles;
    }


}
