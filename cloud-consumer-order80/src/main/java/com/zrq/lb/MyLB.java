package com.zrq.lb;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

//这个代码 只是为了理解负载均衡的一部分操作 有很多漏洞 比如如果我当前没有服务怎么办？ 还是得看源码此部分
@Component
public class MyLB implements LoadBalancer {

    private AtomicInteger atomicInteger = new AtomicInteger(0);

    public final int getAndIncrement() {
        int current;
        int next;
        do {
            current = this.atomicInteger.get();
            //判断是否越界
            next = current >= Integer.MAX_VALUE ? 0 : current + 1;
            //while 这里可以理解成一个自旋锁 CAS的操作，我先获得当前数 如果current 与 next不是我想要的值 进行自旋
        } while (!this.atomicInteger.compareAndSet(current, next));
        System.out.println("******第几次访问，次数next：" + next);
        return next;
    }

    //负载均衡算法：rest接口第几次请求数 % 服务器集群总数量 = 实际调用服务器位置下标  ，每次服务重启动后rest接口计数从1开始。
    @Override
    public ServiceInstance instances(List<ServiceInstance> serviceInstanceList) {
        int index = getAndIncrement() % serviceInstanceList.size();
        return serviceInstanceList.get(index);
    }
}
