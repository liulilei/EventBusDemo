1、概述
EventBus定义：是一个发布 / 订阅的事件总线。

这么说应该包含4个成分：发布者，订阅者，事件，总线。

那么这四者的关系是什么呢？

很明显：订阅者订阅事件到总线，发送者发布事件。

大体应该是这样的关系：



订阅者可以订阅多个事件，发送者可以发布任何事件，发布者同时也可以是订阅者。

好了，大体了解基本的关系以后，我们通过案例驱动来教大家如何使用；

2、代码是最好的老师
相信大家对Fragment都有所了解，现在我们的需求是这样的，两个Fragment组成主界面，左边的Fragment是个目录、即列表，右边的Fragment是详细信息面板；

a、目录的列表是从网络获取的。

b、当点击目录上的条目时，动态更新详细信息面板；

效果图：




看了这个需求，我们传统的做法是：

a、目录Fragment在onCreate中去开启线程去访问网络获取数据，获取完成以后，通过handler去更新界面。

b、在目录的Fragment中提供一个接口，然后详细信息面板去注册这个接口，当发生点击时，去回调这个接口，让详细信息面板发生改变。

其实这种做法也还是不错的，但是有了EventBus之后，我们交互会发生什么样的变化呢？拭目以待吧。

首先提一下：

EventBus.getDefault().register(this);//订阅事件

EventBus.getDefault().post(object);//发布事件

EventBus.getDefault().unregister(this);//取消订阅


1、MainActivity及其布局
[java] view plain copy


package com.angeldevil.eventbusdemo;  
  
import android.os.Bundle;  
import android.support.v4.app.FragmentActivity;  
  
public class MainActivity extends FragmentActivity  
{  
    @Override  
    protected void onCreate(Bundle savedInstanceState)  
    {  
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.activity_main);  
    }  
  
}  


[html] view plain copy


<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"  
    xmlns:tools="http://schemas.android.com/tools"  
    android:layout_width="match_parent"  
    android:layout_height="match_parent"  
    android:baselineAligned="false"  
    android:divider="?android:attr/dividerHorizontal"  
    android:orientation="horizontal"  
    android:showDividers="middle" >  
  
    <fragment  
        android:id="@+id/item_list"  
        android:name="com.angeldevil.eventbusdemo.ItemListFragment"  
        android:layout_width="0dip"  
        android:layout_height="match_parent"  
        android:layout_weight="1" />  
  
    <fragment  
        android:id="@+id/item_detail_container"  
        android:name="com.angeldevil.eventbusdemo.ItemDetailFragment"  
        android:layout_width="0dip"  
        android:layout_height="match_parent"  
        android:layout_weight="2" />  
  
</LinearLayout>  


可以看到，我们MainActvity可以说没有一行代码，布局文件即两个Fragment组成；


2、ItemListFragment
首先看个实体类：


[java] view plain copy


package com.angeldevil.eventbusdemo;  
  
import java.util.ArrayList;  
import java.util.List;  
  
public class Item  
{  
    public String id;  
    public String content;  
  
    public static List<Item> ITEMS = new ArrayList<Item>();  
    static  
    {  
        // Add 6 sample items.  
        addItem(new Item("1", "Item 1"));  
        addItem(new Item("2", "Item 2"));  
        addItem(new Item("3", "Item 3"));  
        addItem(new Item("4", "Item 4"));  
        addItem(new Item("5", "Item 5"));  
        addItem(new Item("6", "Item 6"));  
    }  
  
    private static void addItem(Item item)  
    {  
        ITEMS.add(item);  
    }  
  
    public Item(String id, String content)  
    {  
        this.id = id;  
        this.content = content;  
    }  
  
    @Override  
    public String toString()  
    {  
        return content;  
    }  
}  



[java] view plain copy


package com.angeldevil.eventbusdemo;  
  
import android.os.Bundle;  
import android.support.v4.app.ListFragment;  
import android.view.View;  
import android.widget.ArrayAdapter;  
import android.widget.ListView;  
  
import com.angeldevil.eventbusdemo.Event.ItemListEvent;  
  
import de.greenrobot.event.EventBus;  
  
public class ItemListFragment extends ListFragment  
{  
  
    @Override  
    public void onCreate(Bundle savedInstanceState)  
    {  
        super.onCreate(savedInstanceState);  
        // Register  
        EventBus.getDefault().register(this);  
    }  
  
    @Override  
    public void onDestroy()  
    {  
        super.onDestroy();  
        // Unregister  
        EventBus.getDefault().unregister(this);  
    }  
  
    @Override  
    public void onViewCreated(View view, Bundle savedInstanceState)  
    {  
        super.onViewCreated(view, savedInstanceState);  
        // 开启线程加载列表  
        new Thread()  
        {  
            public void run()  
            {  
                try  
                {  
                    Thread.sleep(2000); // 模拟延时  
                    // 发布事件，在后台线程发的事件  
                    EventBus.getDefault().post(new ItemListEvent(Item.ITEMS));  
                } catch (InterruptedException e)  
                {  
                    e.printStackTrace();  
                }  
            };  
        }.start();  
    }  
  
    public void onEventMainThread(ItemListEvent event)  
    {  
        setListAdapter(new ArrayAdapter<Item>(getActivity(),  
                android.R.layout.simple_list_item_activated_1,  
                android.R.id.text1, event.getItems()));  
    }  
  
    @Override  
    public void onListItemClick(ListView listView, View view, int position,  
            long id)  
    {  
        super.onListItemClick(listView, view, position, id);  
        EventBus.getDefault().post(getListView().getItemAtPosition(position));  
    }  
  
}  


ItemListFragment里面在onCreate里面进行了事件的订阅，onDestroy里面进行了事件的取消；onViewCreated中我们模拟了一个子线程去网络加载数据，获取成功后我们调用

了EventBus.getDefault().post(new ItemListEvent(Item.ITEMS));发布了一个事件；

onListItemClick则是ListView的点击事件，我们调用了EventBus.getDefault().post(getListView().getItemAtPosition(position));去发布一个事件，

getListView().getItemAtPosition(position)的类型为Item类型；

细心的你一定发现了一些诡异的事，直接new Thread()获取到数据以后，竟然没有使用handler；我们界面竟然发生了变化，那么List是何时绑定的数据？

仔细看下代码，发现这个方法：

public void onEventMainThread(ItemListEvent event)
        {
                setListAdapter(new ArrayAdapter<Item>(getActivity(),
                                Android.R.layout.simple_list_item_activated_1,
                                android.R.id.text1, event.getItems()));
        }

应该是这个方法为List绑定的数据。那么这个方法是怎么被调用的呢？

现在就可以细谈订阅事件与发布事件了：

如果方法名以onEvent开头，则代表要订阅一个事件，MainThread意思，这个方法最终要在UI线程执行；当事件发布的时候，这个方法就会被执行。

那么这个事件什么时候发布呢？

我们的onEventMainThread触发时机应该在new Thread()执行完成之后，可以看到子线程执行完成之后，执行了EventBus.getDefault().post(new ItemListEvent(Item.ITEMS));

意味着发布了一个事件，当这个事件发布，我们的onEventMainThread就执行了，那么二者的关联关系是什么呢？

其实和参数的类型，我们onEventMainThread需要接收一个ItemListEvent ，我们也发布了一个ItemListEvent的实例。

现在我们完整的理一下：

在onCreate里面执行        EventBus.getDefault().register(this);意思是让EventBus扫描当前类，把所有onEvent开头的方法记录下来，如何记录呢？使用Map，Key为方法的参数类型，Value中包含我们的方法。

这样在onCreate执行完成以后，我们的onEventMainThread就已经以键值对的方式被存储到EventBus中了。

然后当子线程执行完毕，调用EventBus.getDefault().post(new ItemListEvent(Item.ITEMS))时，EventBus会根据post中实参的类型，去Map中查找对于的方法，于是找到了我们的onEventMainThread，最终调用反射去执行我们的方法。

现在应该明白了，整个运行的流程了；那么没有接口却能发生回调应该也能解释了。

现在我们在看看代码，当Item点击的时候EventBus.getDefault().post(getListView().getItemAtPosition(position));我们同样发布了一个事件，参数为Item；这个事件是为了让详细信息的Fragment去更新数据，不用说，按照上面的推测，详细信息的Fragment里面一个有个这样的方法：        public void onEventMainThread(Item item) ； 是不是呢？我们去看看。

3、ItemDetailFragment
[java] view plain copy


package com.angeldevil.eventbusdemo;  
  
import android.os.Bundle;  
import android.support.v4.app.Fragment;  
import android.view.LayoutInflater;  
import android.view.View;  
import android.view.ViewGroup;  
import android.widget.TextView;  
import de.greenrobot.event.EventBus;  
  
public class ItemDetailFragment extends Fragment  
{  
  
    private TextView tvDetail;  
  
    @Override  
    public void onCreate(Bundle savedInstanceState)  
    {  
        super.onCreate(savedInstanceState);  
        // register  
        EventBus.getDefault().register(this);  
    }  
  
    @Override  
    public void onDestroy()  
    {  
        super.onDestroy();  
        // Unregister  
        EventBus.getDefault().unregister(this);  
    }  
  
    /** List点击时会发送些事件，接收到事件后更新详情 */  
    public void onEventMainThread(Item item)  
    {  
        if (item != null)  
            tvDetail.setText(item.content);  
    }  
  
    @Override  
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  
            Bundle savedInstanceState)  
    {  
        View rootView = inflater.inflate(R.layout.fragment_item_detail,  
                container, false);  
        tvDetail = (TextView) rootView.findViewById(R.id.item_detail);  
        return rootView;  
    }  
}  


果然不出我们的所料，真的存在onEventMainThread(Item item)的方法。当然了，必须在onCreate里面首先书写EventBus.getDefault().register(this);让EventBus扫描再说。


那么这个Fragment的流程就是：onCreate时，EventBus扫描当前类，将onEventMainThread以键值对的形式进行存储，键为Item.class ，值为包含该方法的对象。

然后当ItemListFragment中Item被点击时，发布了一个事件：EventBus.getDefault().post(getListView().getItemAtPosition(position));实参的类型恰好是Item，于是触发我们的

onEventMainThread方法，并把Item实参传递进来，我们更新控件。

4、Event
这里还有个事件类：


[java] view plain copy


package com.angeldevil.eventbusdemo;  
  
import java.util.List;  
  
public class Event  
{  
    /** 列表加载事件 */  
    public static class ItemListEvent  
    {  
        private List<Item> items;  
  
        public ItemListEvent(List<Item> items)  
        {  
            this.items = items;  
        }  
  
        public List<Item> getItems()  
        {  
            return items;  
        }  
    }  
  
}  

ItemListEvent我们在ItemListFragment中使用的，作为的是onEventMainThread中的参数。封装这一层数据类 主要是为了对事件传递时

的参数进行区分，可以在整个Event类中定义不同的类来当做事件，并且把自己需要的值传入，当订阅者收到消息的时候可以直接拿来使用



到此我们的EventBus的初步用法就介绍完毕了。纵观整个代码，木有handler、木有AsynTask，木有接口回调；but，我们像魔术般的实现了我们的需求；来告诉我，什么是耦合，没见到~~~


3、EventBus的ThreadMode
EventBus包含4个ThreadMode：PostThread，MainThread，BackgroundThread，Async

MainThread我们已经不陌生了；我们已经使用过。

具体的用法，极其简单，方法名为：onEventPostThread， onEventMainThread，onEventBackgroundThread，onEventAsync即可

具体什么区别呢？

onEventMainThread代表这个方法会在UI线程执行

onEventPostThread代表这个方法会在当前发布事件的线程执行

BackgroundThread这个方法，如果在非UI线程发布的事件，则直接执行，和发布在同一个线程中。如果在UI线程发布的事件，则加入后台任务队列，使用线程池一个接一个调用。

Async 加入后台任务队列，使用线程池调用，注意没有BackgroundThread中的一个接一个。


4、EventBus 3.0 
上述描述的是EventBus 2.x的使用方法，在3.0使用了注解的方式来查找onEvent在activity或者fragment里面的方法。

@Subscribe(threadMode = ThreadMode.MainThread) //在ui线程执行
public void onUserEvent(UserEvent event) {}



@Subscribe(threadMode = ThreadMode.BackgroundThread) //在后线程执行
public void onUserEvent(UserEvent event) {    }



@Subscribe(threadMode = ThreadMode.Async) //强制在后台执行
public void onUserEvent(UserEvent event) {    }



@Subscribe(threadMode = ThreadMode.PostThread) //默认方式, 在发送线程执行
public void onUserEvent(UserEvent event) {    }
其他的用法基本不变。


转载于： http://blog.csdn.net/lmj623565791/article/details/40794879，本文出自：【张鸿洋的博客】

更有EventBus 的源码详细讲解 也可以参考：

转载于：http://blog.csdn.net/lmj623565791/article/details/40920453，本文出自：【张鸿洋的博客】
