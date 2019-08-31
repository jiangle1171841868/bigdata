package com.itheima;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.itheima.bean.Person;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.*;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * @program: etl
 * @description:
 * @author: Mr.Jiang
 * @create: 2019-08-17 20:20
 **/
public class ELKTest {


    private TransportClient client;

    @Before
    public void initCilent() throws UnknownHostException {

        //创建客户端连接
        //参数:Settings settings
        //创建setting对象
        Settings myes = Settings.builder().put("cluster.name", "myes").build();
        client = new PreBuiltTransportClient(myes)
                //添加集群ip地址
                // addTransportAddress参数:TransportAddress transportAddress
                //TransportAddress参数:InetAddress address, int port
                .addTransportAddress(new TransportAddress(InetAddress.getByName("node01"), 9300))
                .addTransportAddress(new TransportAddress(InetAddress.getByName("node02"), 9300))
                .addTransportAddress(new TransportAddress(InetAddress.getByName("node03"), 9300));
    }

    /**
     * 插入json数据
     */
    @Test
    public void putJsonData() {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", "张三");
        jsonObject.put("age", 20);
        jsonObject.put("address", "北京");

        //将json转化为字符串
        String jsonString = JSON.toJSONString(jsonObject);

        //发送数据到es
        client.prepareIndex("test", "demo", "1")
                .setSource(jsonString, XContentType.JSON)
                .get();
    }

    /**
     * 插入map数据
     */
    @Test
    public void putMapData() {

        //准备数据
        Map<String, Object> map = new HashMap<>();
        map.put("name", "萧炎");
        map.put("age", 20);
        map.put("address", "斗破大陆");

        //发送到es
        client.prepareIndex("test", "demo", "2")
                .setSource(map)
                .get();
    }


    /**
     * 发送XContentBuilder数据
     */
    @Test
    public void xContentBuilderData() throws IOException {

        //发送到es
        client.prepareIndex("test", "demo", "3")
                //参数XContentType xContentType, Object... source
                .setSource(new XContentFactory().jsonBuilder()
                        .startObject()
                        .field("name", "刘能")
                        .field("age", 30)
                        .field("address", "辽宁")
                        .endObject())
                .get();
    }

    /**
     * 批量发送数据
     */
    @Test
    public void bulkInsertData() {

        //封装数据
        Person person = new Person();
        person.setName("葫芦娃2");
        person.setAge(22);
        person.setAddress("胡芦村2");
        String personStr = JSON.toJSONString(person);
        HashMap<String, Object> map = new HashMap<>();
        map.put("name", "xiaoli");
        map.put("age", 20);
        map.put("address", "上海");

        //批量发送数据
        BulkRequestBuilder bulkRequestBuilder = client.prepareBulk();

        //创建发送数据对象
        IndexRequestBuilder indexRequestBuilder = client.prepareIndex("test", "demo", "4")
                .setSource(personStr);

        IndexRequestBuilder indexRequestBuilder1 = client.prepareIndex("test", "demo", "5")
                .setSource(map);

        //封装数据到bulkRequestBuilder 批量发送
        //参数ndexRequestBuilder对象
        bulkRequestBuilder.add(indexRequestBuilder);
        bulkRequestBuilder.add(indexRequestBuilder1);

        //执行
        bulkRequestBuilder.get();
    }

    /**
     * 初始化一批数据到索引库当中去准备做查询使用
     * 注意这里初始化的时候，需要给我们的数据设置分词属性
     *
     * @throws Exception
     */
    @Test
    public void createIndexBatch() throws Exception {

        Settings settings = Settings
                .builder()
                .put("cluster.name", "myes") //节点名称， 在es配置的时候设置
                //自动发现我们其他的es的服务器
                .put("client.transport.sniff", "true")
                .build();
        //创建客户端
        TransportClient client = new PreBuiltTransportClient(settings)
                .addTransportAddress(new TransportAddress(InetAddress.getByName("node01"), 9300));//以本机作为节点
        //创建映射
        XContentBuilder mapping = XContentFactory.jsonBuilder()
                .startObject()
                .startObject("properties")
                //      .startObject("m_id").field("type","keyword").endObject()
                .startObject("id").field("type", "integer").endObject()
                .startObject("name").field("type", "text").field("analyzer", "ik_max_word").endObject()
                .startObject("age").field("type", "integer").endObject()
                .startObject("sex").field("type", "text").field("analyzer", "ik_max_word").endObject()
                .startObject("address").field("type", "text").field("analyzer", "ik_max_word").endObject()
                .startObject("phone").field("type", "text").endObject()
                .startObject("email").field("type", "text").endObject()
                .startObject("say").field("type", "text").field("analyzer", "ik_max_word").endObject()
                .endObject()
                .endObject();
        //indexsearch：索引名   mysearch：类型名（可以自己定义）
        PutMappingRequest putmap = Requests.putMappingRequest("indexsearch").type("mysearch").source(mapping);
        //创建索引
        client.admin().indices().prepareCreate("indexsearch").execute().actionGet();
        //为索引添加映射
        client.admin().indices().putMapping(putmap).actionGet();


        BulkRequestBuilder bulkRequestBuilder = client.prepareBulk();
        Person lujunyi = new Person(2, "玉麒麟卢俊义", 28, 1, "水泊梁山", "17666666666", "lujunyi@itcast.com", "hello world今天天气还不错");
        Person wuyong = new Person(3, "智多星吴用", 45, 1, "水泊梁山", "17666666666", "wuyong@itcast.com", "行走四方，抱打不平");
        Person gongsunsheng = new Person(4, "入云龙公孙胜", 30, 1, "水泊梁山", "17666666666", "gongsunsheng@itcast.com", "走一个");
        Person guansheng = new Person(5, "大刀关胜", 42, 1, "水泊梁山", "17666666666", "wusong@itcast.com", "我的大刀已经饥渴难耐");
        Person linchong = new Person(6, "豹子头林冲", 18, 1, "水泊梁山", "17666666666", "linchong@itcast.com", "梁山好汉");
        Person qinming = new Person(7, "霹雳火秦明", 28, 1, "水泊梁山", "17666666666", "qinming@itcast.com", "不太了解");
        Person huyanzhuo = new Person(8, "双鞭呼延灼", 25, 1, "水泊梁山", "17666666666", "huyanzhuo@itcast.com", "不是很熟悉");
        Person huarong = new Person(9, "小李广花荣", 50, 1, "水泊梁山", "17666666666", "huarong@itcast.com", "打酱油的");
        Person chaijin = new Person(10, "小旋风柴进", 32, 1, "水泊梁山", "17666666666", "chaijin@itcast.com", "吓唬人的");
        Person zhisheng = new Person(13, "花和尚鲁智深", 15, 1, "水泊梁山", "17666666666", "luzhisheng@itcast.com", "倒拔杨垂柳");
        Person wusong = new Person(14, "行者武松", 28, 1, "水泊梁山", "17666666666", "wusong@itcast.com", "二营长。。。。。。");

        bulkRequestBuilder.add(client.prepareIndex("indexsearch", "mysearch", "1")
                .setSource(JSONObject.toJSONString(lujunyi), XContentType.JSON)
        );
        bulkRequestBuilder.add(client.prepareIndex("indexsearch", "mysearch", "2")
                .setSource(JSONObject.toJSONString(wuyong), XContentType.JSON)
        );
        bulkRequestBuilder.add(client.prepareIndex("indexsearch", "mysearch", "3")
                .setSource(JSONObject.toJSONString(gongsunsheng), XContentType.JSON)
        );
        bulkRequestBuilder.add(client.prepareIndex("indexsearch", "mysearch", "4")
                .setSource(JSONObject.toJSONString(guansheng), XContentType.JSON)
        );
        bulkRequestBuilder.add(client.prepareIndex("indexsearch", "mysearch", "5")
                .setSource(JSONObject.toJSONString(linchong), XContentType.JSON)
        );
        bulkRequestBuilder.add(client.prepareIndex("indexsearch", "mysearch", "6")
                .setSource(JSONObject.toJSONString(qinming), XContentType.JSON)
        );
        bulkRequestBuilder.add(client.prepareIndex("indexsearch", "mysearch", "7")
                .setSource(JSONObject.toJSONString(huyanzhuo), XContentType.JSON)
        );
        bulkRequestBuilder.add(client.prepareIndex("indexsearch", "mysearch", "8")
                .setSource(JSONObject.toJSONString(huarong), XContentType.JSON)
        );
        bulkRequestBuilder.add(client.prepareIndex("indexsearch", "mysearch", "9")
                .setSource(JSONObject.toJSONString(chaijin), XContentType.JSON)
        );
        bulkRequestBuilder.add(client.prepareIndex("indexsearch", "mysearch", "10")
                .setSource(JSONObject.toJSONString(zhisheng), XContentType.JSON)
        );
        bulkRequestBuilder.add(client.prepareIndex("indexsearch", "mysearch", "11")
                .setSource(JSONObject.toJSONString(wusong), XContentType.JSON)
        );

        bulkRequestBuilder.get();
        client.close();

    }


    /**
     * 通过id查询
     */
    @Test
    public void queryById() {

        GetResponse documentFields = client.prepareGet("indexsearch", "mysearch", "1").get();
        System.out.println("documentFields = " + documentFields.getSourceAsString());
        //Map<String, Object> source = documentFields.getSource();

    }

    /**
     * 查询所有数据
     */
    @Test
    public void queryAll() {
        SearchResponse searchResponse = client.prepareSearch("indexsearch")
                .setTypes("mysearch")
                //参数:QueryBuilder queryBuilder 接口通过QueryBuilders方法来创建实现类
                .setQuery(QueryBuilders.matchAllQuery())
                .get();
        parseHit(searchResponse);
    }

    /**
     * range范围查询
     * 查询年龄大于
     */
    @Test
    public void rangeQuery() {

        RangeQueryBuilder age = QueryBuilders.rangeQuery("age").lte(30).gte(18);
        getSearchResponse(age);

    }

    /**
     * 词条查询
     */
    @Test
    public void termQuery() {
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("name", "林冲");
        getSearchResponse(termQueryBuilder);
    }

    /**
     * 模糊匹配
     * 最大匹配二次  默认配置一次
     */
    @Test
    public void fuzzQuery() {

        FuzzyQueryBuilder fuzziness = QueryBuilders.fuzzyQuery("say", "helol").fuzziness(Fuzziness.TWO);
        getSearchResponse(fuzziness);
    }

    /**
     * 通配符查询
     */
    @Test
    public void wildcardQuery() {

        WildcardQueryBuilder name = QueryBuilders.wildcardQuery("name", "林*");
        getSearchResponse(name);

    }

    /**
     * 复合查询
     */
    @Test
    public void boolQuery() {

        //查询年龄是18到28
        RangeQueryBuilder age = QueryBuilders.rangeQuery("age").gt(18).lt(28);
        //男性
        TermQueryBuilder sex = QueryBuilders.termQuery("sex", "1");
        //id范围在10到13范围内的
        RangeQueryBuilder id = QueryBuilders.rangeQuery("id").gte(10).lte(13);

        //构建复合查询
        BoolQueryBuilder should = QueryBuilders.boolQuery().should(id).should(QueryBuilders.boolQuery().must(age).must(sex));

        getSearchResponse(should);

    }

    /**
     * 分页查询
     */
    @Test
    public void pageQuery() {
        SearchRequestBuilder search = getSearch();
        //查询全部数据
        SearchResponse age = search.setQuery(QueryBuilders.matchAllQuery())
                //添加排序
                .addSort("age", SortOrder.DESC)
                //设置起始页数
                .setFrom(0)
                //设置查询的条数
                .setSize(5)
                .get();

        parseHit(age);

    }


    @Test
    public void highlightQuer(){

        //创建词条查询对象
        SearchRequestBuilder searchRequestBuilder = getSearch().setQuery(QueryBuilders.termQuery("say", "hello"));

        //创建高亮显示对象
        //设置高亮 1.高亮的字段前缀 后缀
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("say")
                .preTags("<font color='red'>")
                .postTags("</font>");

        //封装高亮显示对象
        SearchResponse searchResponse = searchRequestBuilder.highlighter(highlightBuilder).get();

        //获取内层hits
        SearchHit[] hits = searchResponse.getHits().getHits();
        for (SearchHit hit : hits) {
            //获取整条文档
            System.out.println(hit.getSourceAsString());
            //获取高亮数据
            Text[] says = hit.getHighlightFields().get("say").getFragments();
            for (Text say : says) {
                System.out.println("高亮数据："+say);
            }
        }
    }

    /**
     * 根据id修改索引
     */
    @Test
    public void updateByID(){

        //准备数据
        Person person= new Person(5, "宋江", 88, 0, "水泊梁山", "17666666666","wusong@itcast.com","及时雨宋江");

        //转化为json
        String s = JSON.toJSONString(person);
        client.prepareUpdate("indexsearch","mysearch","1")
                //需要加XContentType.JSON 不然操作不成功
        .setDoc(s,XContentType.JSON)
        .get();
    }

    /**
     * 根据id删除
     */
    @Test
    public void delByID(){

        client.prepareDelete("indexsearch","mysearch","1").get();
    }

    /**
     * 根据过滤删除数据
     */
    @Test
    public void delByRangeAge() {

        DeleteByQueryAction.INSTANCE.newRequestBuilder(client)
                .source("indexsearch")
                .filter(QueryBuilders.rangeQuery("age").gte(18).lte(20))
                .get();
    }

    /**
     * 删除索引库
     */
    @Test
    public void delindex(){
        client.admin().indices().prepareDelete("test").get();

    }
        @After
    public void close() {
        if (client != null) {
            client.close();
        }
    }

    public void parseHit(SearchResponse searchResponse) {
        //获取数据  两层hits
        SearchHit[] hits = searchResponse.getHits().getHits();
        for (SearchHit hit : hits) {
            System.out.println("hit = " + hit.getSourceAsString());

        }
    }

    public void getSearchResponse(QueryBuilder QueryBuilder) {

        SearchResponse searchResponse = client.prepareSearch("indexsearch")
                .setTypes("mysearch")
                .setQuery(QueryBuilder)
                .get();
        parseHit(searchResponse);
    }

    public SearchRequestBuilder getSearch() {

        SearchRequestBuilder searchResponse = client.prepareSearch("indexsearch")
                .setTypes("mysearch");

        return searchResponse;
    }

}
