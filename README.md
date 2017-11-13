# Network
<ul>
<li>Network library with the ability to induce dependency on the request queues.
<li>Operations are seperated from requests, you can do multiple requests and time consuming codes in a single operation.
<li>You can use this library for any type of time consuming operations and not necessarily network requests.
</ul>
<p>There are two main classes: <code>NetworkOperaion</code> and <code>NetworkOperator</code>. You get the singleton instance of <code>NetworkOperator</code> and then post <code>NetworkOperations</code> on it to execute them.</p>

## Usage

Add the dependency:
```Groovy
dependencies {
	compile 'com.yashoid:network:1.3.0'
}
```

<h2>Network Operator</h2>
There are three queues for running the operations.
<ul>
<li><p><b>UI content</b> Is the queue with the most importance. As long as there is some operation on this queue. No other operations will be executed.</p></li>
<li><p><b>User action</b> This queue has next level of importance. As long as there is some operation on this queue. No background operation will be executed.</p></li>
<li><p><b>Background</b> Operations on this queue will only be executed if there are no other operations left to be execuuted.</p></li>
</ul>
<p>If some operation is being executed on a lower importance queue and some new operation is sent to a more important queue, the new operation will be executed in parallel with the current operation(s). After the less important operation(s) is finished, the less important queue(s) will wait for the more important one to get free.</p>

<h2>Network Operation</h2>
<p>Each operation has a type that identifies the operation's queue and a priority. Operations with higher priority in a queue will be executed before operations with a lower priority.</p>
<p>Network operations structure is very similar to an <code>AsyncTask</code>. <code>operate()</code> will be called on a thread and <code>onOperationFinished()</code> will be called on the Main Thread. <code>publishProgress</code> and <code>onProgressUpdate()</code> are exactly similar to <code>AsyncTask</code>.</p>

<h2>Network Request</h2>
<p>There are subclasses of <code>NetworkRequest</code> class to perform API calls. Using network requests is optional.</p>
<h4>Available network requests</h4>
<ul>
<li>JsonObjectRequest</li>
<li>JsonArrayRequest</li>
<li>JsonReaderRequest</li>
<li>FileRequest</li>
<li>StringReques</li>
</ul>
<p>There are useful methods to set the body for the request. Using them you can set different types of objects as the body that includes: JsonObject, JsonArray, String, InputStream, File. Calling <code>setBody()</code> will automatically change the request method to POST.</p>
<p>In addition you can set the method of the request which you can use to set non-standard methods (HttpUrlRequest normally throws a runtime exception). Also there is a method to directly access the underlying <code>HttpUrlRequest</code> for other uses like setting request headers.</p>
<p><i><b>Attention</b> Getting request's response code is valid only after you have executed the request. In case you are using <code>JsonReaderRequest</code> remember to call <code>disconnect()</code> after you have finished using the <code>JsonReader</code>. For other requests disconnect is called by default.</i></p>
