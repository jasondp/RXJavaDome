package com.rxjavatest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;


public class MainActivity extends AppCompatActivity {
    @Bind(R.id.main_activity_test_tv)
    TextView mTv;

    private Observable<String> threadObservable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.main_activity_hello_rx_bt)
    public void helloRXJava() {
        //被观察者
        Observable<String> helloRXJavaObservable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                e.onNext("Jason");
                e.onNext("Tom");
                e.onComplete();
            }
        }).observeOn(AndroidSchedulers.mainThread());

        //观察者
        Observer<String> observer = new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(String value) {
                Log.e("jason", "hello RxJava:" + value);
                mTv.setText(value);
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        };

        //给被观察者订阅观察者
        helloRXJavaObservable.subscribe(observer);

        //链式调用
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                e.onNext("张哲华");
                e.onComplete();
            }
        }).subscribe(new Observer<String>() {
            Disposable dis;

            @Override
            public void onSubscribe(Disposable disposable) {
                dis = disposable;
            }

            @Override
            public void onNext(String value) {
                Log.e("jason", value);
                dis.dispose();//用于终止接受事件
                dis.isDisposed();//返回是否停止接受事件
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    @OnClick(R.id.main_activity_rx_bt)
    public void testSubscribe() {

        //单个参数的
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                e.onNext("jason");
                e.onComplete();
            }
        }).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                Log.e("jason", s);
            }
        });

        //无参数的其实应该是没什么用目前不知道有什么特殊意义
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                e.onNext("jason");
            }
        }).subscribe();
    }


    @OnClick(R.id.main_activity_rxthread_rx_bt)
    public void threadTest() {

        threadObservable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                e.onNext(Thread.currentThread().getName());
                e.onComplete();
            }
        }).subscribeOn(Schedulers.newThread());

        Observer<String> threadObserver = new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(String value) {
                Log.e("jason", "thread Observable :" + value);
                Log.e("jason", "thread Observer:" + Thread.currentThread().getName());
                //                输出的结果
                //                E/jason: thread Observable :RxNewThreadScheduler-1
                //                E/jason: thread Observer:RxNewThreadScheduler-1
                //总结在同一条线程
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        };
        threadObservable.subscribe(threadObserver);
    }


    @OnClick(R.id.main_activity_thread_manager_rx_bt)
    public void ioToMainThread() {
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                e.onNext(Thread.currentThread().getName());
            }
        }).subscribeOn(Schedulers.newThread())//子线程发送
                .observeOn(AndroidSchedulers.mainThread())//主线程接受
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(String value) {
                        Log.e("jason", "Observable Thread:" + value);
                        Log.e("jason", "observer Thread :" + Thread.currentThread().getName());
                        //输出结果
                        // E/jason: Observable Thread:RxNewThreadScheduler-1
                        // E/jason: observer Thread :main
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    @OnClick(R.id.main_activity_map_rx_bt)
    public void mapUse() {
        //map操作符是将被观察者的时间按照一定的函数去改变后发送给观察者
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                e.onNext("jason");
            }
        }).map(new Function<String, User>() {
            @Override
            public User apply(String s) throws Exception {
                User user = new User();
                user.setName(s);
                user.setPassword("kabsdkjbaskjdb");
                return user;
            }
        }).subscribe(new Consumer<User>() {
            @Override
            public void accept(User user) throws Exception {
                Log.e("jason", user.toString());
                //输出结果
                //E/jason: User{name='jason', password='kabsdkjbaskjdb'}
            }
        });
    }

    @OnClick(R.id.main_activity_flat_map_rx_bt)
    public void flatMapUse() {
        //flatMap:将被观察者的事件转变成多少个被观察者发送的事件,然后将他们合并在一个被观察者的时间中一起发送出去
        //flatMap是无序的如果想要有序的可以使用concatMap
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                e.onNext(1);
                e.onNext(2);
                e.onComplete();
            }
        }).flatMap(new Function<Integer, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(Integer integer) throws Exception {
                final List<String> list = new ArrayList<>();
                for (int i = 0; i < 2; i++) {
                    list.add("jason " + integer.intValue());
                }
                // delay(10, TimeUnit.MILLISECONDS)延时
                // Observable.fromIterable(list)将事件放在一个observable中
                return Observable.fromIterable(list).delay(10, TimeUnit.MILLISECONDS);
            }
        }).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                Log.e("jason", s);
                //输出结果
                //E / jason:jason 1
                //E / jason:jason 1
                //E / jason:jason 2
                //E / jason:jason 2
            }
        });
    }

    @OnClick(R.id.main_activity_concat_map_rx_bt)
    public void concatMapTest() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                e.onNext(1);
                e.onNext(2);
                e.onNext(3);
                e.onComplete();

            }
        }).concatMap(new Function<Integer, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(Integer integer) throws Exception {
                List<String> list = new ArrayList<String>();
                for (int i = 0; i < 3; i++) {
                    list.add("I am " + integer.intValue());
                }
                return Observable.fromIterable(list);
            }
        }).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                Log.i("jason", s);
                //输出结果
                //               I/jason: I am 1
                //               I/jason: I am 1
                //               I/jason: I am 1
                //               I/jason: I am 2
                //               I/jason: I am 2
                //               I/jason: I am 2
                //               I/jason: I am 3
                //               I/jason: I am 3
                //               I/jason: I am 3
            }
        });
    }

    @OnClick(R.id.main_activity_zip_rx_bt)
    public void zipTest() {

        //Zip通过一个函数将多个Observable发送的事件结合到一起，然后发送这些组合到一起的事件.
        // 它按照严格的顺序应用这个函数。它只发射与发射数据项最少的那个Observable一样多的数据。
        Observable<Integer> objectObservable = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                e.onNext(1);
                e.onNext(2);
                e.onNext(3);
                e.onNext(4);
                e.onNext(5);
                e.onComplete();
            }
        }).subscribeOn(Schedulers.io());

        Observable<String> objectObservable1 = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                e.onNext("hhh");
                e.onNext("xxx");
                e.onComplete();
            }
        }).subscribeOn(Schedulers.io());

        Observable.zip(objectObservable, objectObservable1, new BiFunction<Integer, String, String>() {
            @Override
            public String apply(Integer integer, String s) throws Exception {
                return integer + s;
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                Log.i("jason", s);
                //                输出结果
                //                I/jason: 1hhh
                //                I/jason: 2xxx
            }
        });
    }

    @OnClick(R.id.main_activity_why_back_pressure_rx_bt)
    public void why() {
        //会出现oom
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                for (int i = 0; ; i++) {
                    e.onNext(i);
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Log.i("jason", integer + "");
                    }
                });
    }


    @OnClick(R.id.main_activity_fix_oom_rx_bt)
    public void fixUpQuestion() {
        //使用自己的方法解决上面的问题
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                int i = 0;
                while (true) {
                    e.onNext(i++);
                    Thread.sleep(2000);
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Log.i("jason", integer + "");
                    }
                });
    }

    @OnClick(R.id.main_activity_back_pressure_rx_bt)
    public void backPressureTest() {
        //why we use back pressure?
    }
}
