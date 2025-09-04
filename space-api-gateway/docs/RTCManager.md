I have event types: InIt, Finish.

In a kotlin flow, how do I ensure FinishSync event is right after and InitSync. If there is a different type of event
than InitSync event before FinishSync event, I will prevent next process. This's mean there can't be two consecutive
FinishSync events.

Here, I'm using Ktor as a server. Listen to event through sockets