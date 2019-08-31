package com.itheima.spark

//todo 网络传输需要实现序列化接口
trait RemoteMessage extends Serializable {}

//todo 定义worker注册的样例类 需要网络传输 需要实现序列化接口
case class RegisterMessage(val workerID: String, val memory: Int, val cores: Int) extends RemoteMessage

//todo 定义master回复注册的样例类 需要网络传输 需要实现序列化接口
case class ReplyMessage(message: String) extends RemoteMessage

//todo 定义心跳样例类 发送给自己 不需要参数(使用object就可以) 不需要网络传递 不需要实现序列化接口
case object HeartBeat

//todo 发送心跳 需要网络传输 需要实现序列化接口
case class SendHeartBeat(workerID: String) extends RemoteMessage

//todo 检查是否超时 发送给自己 不需要参数(使用object就可以) 不需要网络传递 不需要实现序列化接口
object CheckOutTime

