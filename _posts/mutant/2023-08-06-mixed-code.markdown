---
layout: posts
title:  I want to talk to your managed code
date:   2023-08-06
categories: mutant
---

**TL;DR**  
A few experiments about mixed managed/unmanaged assemblies. To begin with, we start by presenting a C# programme that hides a part of its payload in an unmanaged C++ stub as `obj` file, and interact with. Loading it into `dnSpy` will show that part of the code is unmanaged and cannot be displayed properly. We take it one step further by removing the references to the UNmanaged code and changing the EXE entry point. The programme then executes the unamanaged code and we leverage the power of `ICLRRuntimeHost` to get back to the C# part. Since there is no reference to the unmanaged code, `dnSpy` shows nothing but the C#.

## 0. Intro

As a lazy reverse engineer, facing a .NET binary would immediately make me fire up `dnSpy` to dive into the code. Seeing something as simple as this would not interest me that much at first sight.

<img style="margin-left:auto;margin-right:auto;display:block;" alt="dnspy - no unmanaged" src="/assets/res/mutant/mixed-code/dnspy_nounmanaged.png">

Only one namespace, one class, and two dummy routines. I would assume that the binary only prints _Hey, I just met you, and this crazy_, and quickly terminates. But by running it, I would see something really strange, with unexpected messages:

<img style="margin-left:auto;margin-right:auto;display:block;" alt="exec - no unmanaged" src="/assets/res/mutant/mixed-code/exec_nounmanaged.png">

Wait, what ? 0.o Where do these strings comes from ?! Let's dive into the fabulous world of mixed-mode assemblies, and be ready for a what-the-f***-is-this-shit-ness overload ...

## 1. Managed/unmanaged

C# is a really handy language (still not my favourite, though, python 2.7 you're still my love) but for malware authors, it has a significant drawback: it is fairly easy to reverse engineer. Tools such as [`dnSpy`](https://github.com/dnSpy/dnSpy) are able to provide a source code version quite akin to the original one, and make analysis much easier than reading bits and bytes. Instead of being directly compiled to machine code, C# is translated into an intermediary language named CIL (formerly named IL, for ...  _Intermediary Language_), and then just-in-time compiled in order to run in the .NET environment.

The code is referred to as _managed code_, meaning that it expects to run under the management of a _Common Language Infrastructure (CLI)_, such as _.NET Framework_ or _Common Language Runtime (CLR)_. _Common Language Infrastructure_ is an open specification that describes how a runtime environment could allow multiple languages to be used on different computing platforms, without being rewritten. To rephrase it with simple words, _managed code_ is just a code whose execution is handled by a runtime (CLR is this case), turning managed code into machine code, and executing it. The advantage of machine code is the interoperability between languages, and the fact that the burden of memory management is not left to the developer (well, not entirely at least).

On the other hand, the _unmanaged code_ (written in C, C++ for instance), has the advantage that it is directly turned into machine language, and cannot be decompiled into meaningful words, compared to C#. C++ is not supposed to be managed, but the variant C++/CLI came as a means to better connect .NET Framework and C++. Simply said, C++/CLI is a version of C++, modified in order to run in CLI. It therefore provides ways to interact with other .NET languages, such as C#.

Knowing that C++ programs can call C functions, we therefore have a way to pass from C# to C ...

## 2. From C# to C++

To make a C++ library able to interact with .NET platform, one has to add the switch `/clr` to the compiling command. This would make the module able to benefit from the .NET features, while still being compatible with the rest. While linking, another interesting flag, known as `/CLRIMAGETYPE:IJW`, must be added. Also known as _C++ Interop_, this humorous flag stands for _It Just Works_. The first `/clr` switch creates managed assembly, and `/ijw` just **_automagically_** makes it usable, like any other managed class.

_Normally, a meme with Todd Howard should have been put here, but Sponge Bob does the job_

<img width="50%" style="margin-left:auto;margin-right:auto;display:block;" alt="IJW" src="/assets/res/mutant/mixed-code/ijw.jpg">

### 2.1. Unmanaged library
The first experiment was to build a C# programme with _"hidden"_ features, that is, features that cannot be easily recovered with `dnSpy`. To do so, I first created a **CLR Class library (.NET Framework)**, with a first C++ class, with the feature I wanted to hide (in this example, executing the command `whoami`).

Class **UnmanagedLib.cpp**

```cpp
#include "UnmanagedLib.h"

void execwhoami() {
	std::array<char, 128> buffer;
	std::unique_ptr<FILE, decltype(&_pclose)> pipe(_popen("whoami", "r"), _pclose);
	if (!pipe) return;
	std::string outbuff;
	while (fgets(buffer.data(), (int)buffer.size(),pipe.get()) != nullptr) {
		outbuff += buffer.data();
	}
	puts(outbuff.c_str());
}
```

Header **UnmanagedLib.h**
```cpp
#pragma once
#include <array>
#include <memory>
#include <string>

void execwhoami();
```

After that, I added a wrapper around this library, meant to be compiled with the famous `/clr` flag. It would offer the possibility to call the function from C# while still making `dnSpy` unable to properly decompile it. I then added a second class named `CInterop`, and left the class empty (except the `#include "CInterop.h"` directive. Also, the `#include "pch.h"` should be removed, if any).

Header **CInterop.h**

```cpp
#pragma once
#include "UnmanagedLib.h"

namespace ManagedConsoleApp {
	ref class CInterop
	{
	public:
		static void doExecWhoami() { execwhoami(); }
	};
}

```

This piece of code wraps the native function inside `doExecWhoami`, which will be made available to C#. The namespace `ManagedConsoleApp` is used on purpose, because it is the one we are going to use in the managed code. This class `CInterop` would then belong to the same namespace. Not mandatory to do it like this, though.

To compile it and create things usable by the C# programme, one better should compile manually (from _Developer PowerShell_ in VisualStudio). The global idea is to compile individual components, and linking at the end. The following command would generate the object file `UnmanagedLib.obj`. We compile without linking yet (`/c`) and creates a multithreaded binary (`/MD`) by using `MSVCRT.lib`:

```powershell
PS > cl.exe /c /MD .\UnmanagedLib.cpp
Microsoft (R) C/C++ Optimizing Compiler Version 19.36.32537 for x86
Copyright (C) Microsoft Corporation.  All rights reserved.

UnmanagedLib.cpp
```

Now, time to compile `CInterop`:

```powershell
PS > cl.exe /clr /LN /MD .\CInterop.cpp .\UnmanagedLib.obj
Microsoft (R) C/C++ Optimizing Compiler Version 19.36.32537
for Microsoft (R) .NET Framework version 4.08.9167.0
Copyright (C) Microsoft Corporation.  All rights reserved.

CInterop.cpp
Microsoft (R) Incremental Linker Version 14.36.32537.0
Copyright (C) Microsoft Corporation.  All rights reserved.

/out:CInterop.netmodule
/dll
/noentry
/noassembly
CInterop.obj
.\UnmanagedLib.obj
```

This command would produce `CInterop.netmodule` (switch `/LN`) and `CInterop.obj`. The three newly created files would be then used during compilation and linking time, to make the C# programme benefit from the function `execwhoami`.

### 2.2. Managed console application

Let's now create another project in VisualStudio, by choosing this time **Console app (.NET Framework)** as a template. The class `Program.cs` is as follows, calling the wrapped `execwhoami`. One can notice here that since `CInterop` class belongs TO the namespace `ManagedConsoleApp`, there is no need to put something before:

```cpp
namespace ManagedConsoleApp
{
    internal class Program
    {
        static void Main(string[] args)
        {
            CInterop.doExecWhoami();
        }
    }
}
```

Let's copy `Cinterop.obj`, `CInterop.netmodule` and `UnmanagedLib.bj` in this folder. Although VisualStudio complains about this unknown name `Cinterop`, `csc.exe` should happily compile:

```powershell
PS > csc.exe /target:module /addmodule:CInterop.netmodule .\Program.cs
Microsoft (R) Visual C# Compiler version 4.6.0-3.23259.8 (c3cc1d0c)
Copyright (C) Microsoft Corporation. All rights reserved.
```

This command instructs the compiler to build a module, adding a reference to `Cinterop.netmodule`. Now it's time to merge everything with `link.exe`:

```powershell
PS > link.exe /LTCG /CLRIMAGETYPE:IJW /ENTRY:ManagedConsoleApp.Program.Main /SUBSYSTEM:console /ASSEMBLYMODULE:Cinterop.netmodule .\CInterop.obj .\UnmanagedLib.obj .\Program.netmodule
Microsoft (R) Incremental Linker Version 14.36.32537.0
Copyright (C) Microsoft Corporation.  All rights reserved.

Generating code
Finished generating code
```

The switches are as follows:
* `/LTCG`: perform whole-programme optimisation
* `/CLRIMAGETYPE`: to make it "just working"
* `/ENTRY`: the programme entry point
* `/SUBSYSTEM`: kind of application: console, window, boot, EFI
* `/ASSEMBLYMODULE`: add this module to the assembly

It should generate a `CInterop.exe` (which can be changed with the optional switch `/OUT:name.exe`). Running it finally tells you who you are (:

<img style="margin-left:auto;margin-right:auto;display:block;" alt="whoami" src="/assets/res/mutant/mixed-code/whoami.png">

Let's load it into `dnSpy`, and see what it tells:

<img style="margin-left:auto;margin-right:auto;display:block;" alt="exp1 - dnspy1" src="/assets/res/mutant/mixed-code/exp1_dnspy1.png">

<img style="margin-left:auto;margin-right:auto;display:block;" alt="exp1 - dnspy2" src="/assets/res/mutant/mixed-code/exp1_dnspy2.png">

However, by clicking on the symbol `<Module>.execwhoami`, one reaches an impasse.

<img style="margin-left:auto;margin-right:auto;display:block;" alt="exp1 - dnspy3" src="/assets/res/mutant/mixed-code/exp1_dnspy3.png">

By doing some conversions, it is even possible to pass arguments between managed and unmanaged code. Just beware of calling conventions.

## 3. I want to talk to your managed code !

While this first example is interesting, a reverse engineer can easily see where something smells fishy, since they will clearly see the link to unmanaged code (although it might have legitimate reasons), and would guess that something may be hidden there. The second experiment takes it further by hiding the module from C#, and still bouncing between managed and unmanaged code. The idea here is to set the entry point to `mainCRTStartup`, and implement a `main` in the unmanaged code. Once executed, the goal is to _"get back"_ to the managed code.

### 3.1 Entry points

.NET EXE files contain in their Cor20 header a field named `EntryPointTokenOrRva`. Let’s take a look at what the doc says (source: [https://www.ntcore.com/files/dotnetformat.htm](https://www.ntcore.com/files/dotnetformat.htm)):

<img style="margin-left:auto;margin-right:auto;display:block;" alt="cor20" src="/assets/res/mutant/mixed-code/cor20.png">

Depending of the value of `Flags`, the entry point is considered as a native or a managed one. By taking a look at what `dnSpy` says, one can see that the flags are clear. The entry point is therefore managed, and it makes sense since it's `Program.Main`. 

<img style="margin-left:auto;margin-right:auto;display:block;" alt="exp2 - dnspy1" src="/assets/res/mutant/mixed-code/exp2_dnspy1.png">

To initialise the CRT, the routine `mainCRTStartup` needs to be called to set everything up, call initialisers, and finally call the developer's `main` routine. It means that declaring a valid `main` routine (that is, a routine with recognisable signature) in the unmanaged code, then setting the entry point to `mainCRTStartup`, and our unmanaged routine will be called.

### 3.2. Managed code

To begin with, let's create a dummy C# console application, that contains nothing more than a `Console.WriteLine` in its `Program` class:

```csharp
using System;

namespace EvilExe
{
    public class Program
    {

        public static void Main(string[] args){
            callMeMaybe("Hey, I just met you, and this is crazy");
        }
        public static int callMeMaybe(String msg)
        {
            Console.WriteLine(msg);
            return 0;
        }
    }
}
```

Compiling it with VisualStudio recipe and loading the binary into `dnSpy`, and one would only see this:

<img style="margin-left:auto;margin-right:auto;display:block;" alt="dnspy - no unmanaged" src="/assets/res/mutant/mixed-code/dnspy_nounmanaged.png">

Running it would print _Hey, I just met you, and this is crazy_ since `Main` would be recognised as the entry point. Fair enough.

### 3.3 Unmanaged code - Main routine

As mentioned earlier, the goal of the unmanaged code is to execute something unexpected and then to execute the legitimate code to make the analyst think that everything went well. To begin with, let's create a new **CLR Class library (.NET Framework)** with a class named `CInterop`. The header only contains one routine, that should be reachable from `mainCRTStartup`:


Header **CInterop.h**

```cpp
#pragma once
#include <cstdio>
#include <metahost.h>
#include <string>
#include <msclr\marshal_cppstd.h>
int __stdcall main(int argc, char** argv,char** env);
```

For the moment, let's add a simple call to `puts` in the `main`

Class **CInterop.cpp**

```cpp
#include "CInterop.h"
using namespace System;
int __stdcall main(int argc, char** argv, char** env) {
	puts("Parle à ma main");
	//callCSharp();
	return 0;
}
```

Individually compiling and linking as we did previously, while setting the entry point to `mainCRTStartup`, should just print _Parle à ma main_ and exit. The message _Hey, I just met you, and this is crazy_ will not be printed since the `EvilExe.Program.Main` will not be called.

For the C++ part:

```powershell
PS> cl.exe /clr /c CInterop.cpp
```

Let's copy the newly obtained `obj` file and compile the C#:

```powershell
PS> csc.exe /target:module Program.cs
PS> link.exe /LTCG /CLRIMAGETYPE:IJW /ENTRY:mainCRTStartup /OUT:EvilEXE.exe Cinterop.obj Program.netmodule 
```

Since we did not add the switch `addmodule`, there is no reason that it appears in `dnSpy`, completely hiding it inside the binary.

### 3.4 Calling C#

However, the challenge is now to "return" to the C# code. Multiple possibilities exist, actually. The one I used here leverages the power of `ICLRRuntimeHost` and friends. Let's now implement the routine `callCSharp` in the unmanaged code.

Thanks to C++/CLI, it is possible to use .NET classes inside C++ code. The technique is not new and even quite well documented (just an example: [https://www.ired.team/offensive-security/code-injection-process-injection/injecting-and-executing-.net-assemblies-to-unmanaged-process](https://www.ired.team/offensive-security/code-injection-process-injection/injecting-and-executing-.net-assemblies-to-unmanaged-process). Or another one: [https://0xpat.github.io/Malware_development_part_9/](https://0xpat.github.io/Malware_development_part_9/). Or yet another one: [https://codingvision.net/calling-a-c-method-from-c-c-native-process](https://codingvision.net/calling-a-c-method-from-c-c-native-process)).

Known as _CLR Hosting_ ([https://learn.microsoft.com/en-us/previous-versions/visualstudio/visual-studio-2008/zaf1h1h5(v=vs.90)](https://learn.microsoft.com/en-us/previous-versions/visualstudio/visual-studio-2008/zaf1h1h5(v=vs.90))), the trick is to host the .NET CLR in a process of our choice, in order to tweak it. As stated by the documentation, the process is transparent for applications that were meant to run in the CLR, the runtime being automatically started by `mscoree.dll`. However, unmanaged applications can host the CLR to benefit from its capabilities and control its features according to their needs.

To begin with, an instance of the CLR is created:

```cpp
void callCSharp() {
    ICLRMetaHost* metaHost = NULL;
    ICLRRuntimeInfo* runtimeInfo = NULL;
    ICLRRuntimeHost* runtimeHost = NULL;
    if (CLRCreateInstance(CLSID_CLRMetaHost, IID_ICLRMetaHost, (LPVOID*)&metaHost) != S_OK) return;
    ...
}
```
Once the host created, the runtime can be created inside. To do so, the routine is `metaHost->GetRuntime(LPCWSTR pwzVersion,  REFIID riid, LPVOID *ppRuntime)`. The first argument is supposed to be the version of the runtime. The latter can be obtained in C# through the property `System::Environment::Version`, and to avoid hardcoding it in the binary, it should be computed at runtime. Conversions must be done to retrieve it:

```cpp
    ...
    System::String^ managed = System::Environment::Version->ToString();
    std::string version = msclr::interop::marshal_as<std::string>(managed);
    std::wstring temp = std::wstring(version.begin(), version.end());
    LPCWSTR wideString = temp.c_str(); //4.0.30319.42000
    ...
```

Depending on the .NET framework version, the result may change. As stated in the doc ([https://learn.microsoft.com/en-us/dotnet/api/system.environment.version?view=net-7.0](https://learn.microsoft.com/en-us/dotnet/api/system.environment.version?view=net-7.0)):

>For the .NET Framework Versions 4, 4.5, 4.5.1, and 4.5.2, the Environment.Version property returns a Version object whose string representation has the form 4.0.30319.xxxxx. For the .NET Framework 4.6 and later versions, and .NET Core versions before 3.0, it has the form 4.0.30319.42000.

However, the routine `metaHost->GetRuntime` expects it as `vXXXXX.YYYY.ZZZZ` (three values prepended with a `'v'`). Let's write a dirty piece of code that builds a suitable version string (probably not the best, but I'm a Python guy, and C#/C++/C give me pimples). If everything went well so far, we can continue by getting a reference to the runtime host thanks to `runtimeInfo->GetInterface`:

```cpp
    ...
    char shortversion[32] = { 0 };
    shortversion[0] = 'v';
    int count = 0;
    for (int i = 0; wcslen(wideString); i++) {
        if (wideString[i] == '.' && ++count == 3) { //if we reach the 3rd '.', then stop
            break;
        }
        shortversion[i+1] = wideString[i];
    }
    wchar_t wtext[32];
    mbstowcs(wtext, shortversion, strlen(shortversion) + 1);
    if (metaHost->GetRuntime(wtext, IID_ICLRRuntimeInfo, (LPVOID*)&runtimeInfo) != S_OK) return;
    if (runtimeInfo->GetInterface(CLSID_CLRRuntimeHost, IID_ICLRRuntimeHost, (LPVOID*)&runtimeHost) != S_OK) return;
    ...
```

The `runtimeHost` could be `started` and if running, the routine `ExecuteInDefaultAppDomain` will be our way to call our C# routine. However, the routine called by `ExecuteInDefaultAppDomain` has to stick to a specific signature, otherwise it would not be recognised:

```cpp
virtual HRESULT STDMETHODCALLTYPE ExecuteInDefaultAppDomain( 
            /* [in] */ LPCWSTR pwzAssemblyPath,
            /* [in] */ LPCWSTR pwzTypeName,
            /* [in] */ LPCWSTR pwzMethodName,
            /* [in] */ LPCWSTR pwzArgument,
            /* [out] */ DWORD *pReturnValue) = 0;
```

_One can better understand why `callMeMaybe` returns a useless 0, now. It must return an integer._

Since the first argument to `ExecuteInDefaultAppDomain` is the DLL or EXE to load, we must first retrieve it thanks to `GetModuleFileNameW`:

```cpp
    ...
    DWORD pReturnValue;
    WCHAR szExeFileName[MAX_PATH];
    GetModuleFileNameW(NULL, szExeFileName, MAX_PATH); //get current binary name
    HRESULT res = runtimeHost->ExecuteInDefaultAppDomain(szExeFileName, L"EvilExe.Program", L"callMeMaybe", L"Here's my number: <censored>", &pReturnValue);
    if (res == S_OK)
    {
       puts("CLR executed successfully\n");
    }
    else {
        printf("Error code: %d\n", res);
    }
}
```

And that's it! Let's compile, run, and admire the result:
<img style="margin-left:auto;margin-right:auto;display:block;" alt="exec - no unmanaged" src="/assets/res/mutant/mixed-code/exec_nounmanaged.png">



