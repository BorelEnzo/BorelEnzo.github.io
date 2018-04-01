## So Stealthy

>During an incident response, we captured the network traffic from a suspected compromised host. Could you help us reverse the installed malware?

We are given a suspicious pcap file, aptly named [suspicious pcap](suspicious.pcap). As usual, we began with HTTP traffic, and found a [suspicious payload](payload.xml) in 
the packet 8013.

The malicious script is as follows:

> ```javascript
>function setversion() {} 
>function debug(s) {} 
>function Trololo(b) { 
>	var yei1Euthoo = new ActiveXObject("System.Text.ASCIIEncoding"); 
> 	var oPohToo1em = yei1Euthoo.GetByteCount_2(b); 
> 	var apeuGho2aa = yei1Euthoo.GetBytes_4(b); 
> 	var xieBaf0eeZ = new ActiveXObject("System.Security.Cryptography.FromBase64Transform"); 
> 	apeuGho2aa = xieBaf0eeZ.TransformFinalBlock(apeuGho2aa, 0, oPohToo1em); 
> 	var do2quaiMie = new ActiveXObject("System.IO.MemoryStream"); 
> 	do2quaiMie.Write(apeuGho2aa, 0, (oPohToo1em / 4) * 3); 
> 	do2quaiMie.Position = 0; 
> 	return do2quaiMie; 
>} 
> 
>var dei0eiFu = "AAEAAAD/////AQAAAAAAAAAEAQAAACJTeXN0ZW0uRGVsZWdhdGVTZXJpYWxpemF0aW9uSG9sZGVy"+ 
>"AwAAAAhEZWxlZ2F0ZQd0YXJnZXQwB21ldGhvZDADAwMwU3lzdGVtLkRlbGVnYXRlU2VyaWFsaXph"+ 
>"dGlvbkhvbGRlcitEZWxlZ2F0ZUVudHJ5IlN5c3RlbS5EZWxlZ2F0ZVNlcmlhbGl6YXRpb25Ib2xk"+ 
>"ZXIvU3lzdGVtLlJlZmxlY3Rpb24uTWVtYmVySW5mb1NlcmlhbGl6YXRpb25Ib2xkZXIJAgAAAAkD"+ 
>"AAAACQQAAAAEAgAAADBTeXN0ZW0uRGVsZWdhdGVTZXJpYWxpemF0aW9uSG9sZGVyK0RlbGVnYXRl"+ 
>"RW50cnkHAAAABHR5cGUIYXNzZW1ibHkGdGFyZ2V0EnRhcmdldFR5cGVBc3NlbWJseQ50YXJnZXRU"+ 
>"eXBlTmFtZQptZXRob2ROYW1lDWRlbGVnYXRlRW50cnkBAQIBAQEDMFN5c3RlbS5EZWxlZ2F0ZVNl"+ 
>"cmlhbGl6YXRpb25Ib2xkZXIrRGVsZWdhdGVFbnRyeQYFAAAAL1N5c3RlbS5SdW50aW1lLlJlbW90"+ 
>"aW5nLk1lc3NhZ2luZy5IZWFkZXJIYW5kbGVyBgYAAABLbXNjb3JsaWIsIFZlcnNpb249Mi4wLjAu"+ 
>"MCwgQ3VsdHVyZT1uZXV0cmFsLCBQdWJsaWNLZXlUb2tlbj1iNzdhNWM1NjE5MzRlMDg5BgcAAAAH"+ 
>"dGFyZ2V0MAkGAAAABgkAAAAPU3lzdGVtLkRlbGVnYXRlBgoAAAANRHluYW1pY0ludm9rZQoEAwAA"+ 
>"ACJTeXN0ZW0uRGVsZWdhdGVTZXJpYWxpemF0aW9uSG9sZGVyAwAAAAhEZWxlZ2F0ZQd0YXJnZXQw"+ 
>"B21ldGhvZDADBwMwU3lzdGVtLkRlbGVnYXRlU2VyaWFsaXphdGlvbkhvbGRlcitEZWxlZ2F0ZUVu"+ 
>"dHJ5Ai9TeXN0ZW0uUmVmbGVjdGlvbi5NZW1iZXJJbmZvU2VyaWFsaXphdGlvbkhvbGRlcgkLAAAA"+ 
>"CQwAAAAJDQAAAAQEAAAAL1N5c3RlbS5SZWZsZWN0aW9uLk1lbWJlckluZm9TZXJpYWxpemF0aW9u"+ 
>"SG9sZGVyBgAAAAROYW1lDEFzc2VtYmx5TmFtZQlDbGFzc05hbWUJU2lnbmF0dXJlCk1lbWJlclR5"+ 
>"cGUQR2VuZXJpY0FyZ3VtZW50cwEBAQEAAwgNU3lzdGVtLlR5cGVbXQkKAAAACQYAAAAJCQAAAAYR"+ 
>"AAAALFN5c3RlbS5PYmplY3QgRHluYW1pY0ludm9rZShTeXN0ZW0uT2JqZWN0W10pCAAAAAoBCwAA"+ 
>"AAIAAAAGEgAAACBTeXN0ZW0uWG1sLlNjaGVtYS5YbWxWYWx1ZUdldHRlcgYTAAAATVN5c3RlbS5Y"+ 
>"bWwsIFZlcnNpb249Mi4wLjAuMCwgQ3VsdHVyZT1uZXV0cmFsLCBQdWJsaWNLZXlUb2tlbj1iNzdh"+ 
>"NWM1NjE5MzRlMDg5BhQAAAAHdGFyZ2V0MAkGAAAABhYAAAAaU3lzdGVtLlJlZmxlY3Rpb24uQXNz"+ 
>"ZW1ibHkGFwAAAARMb2FkCg8MAAAAAB4AAAJNWpAAAwAAAAQAAAD//wAAuAAAAAAAAABAAAAAAAAA"+ 
>"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACAAAAADh+6DgC0Cc0huAFMzSFUaGlzIHByb2dy"+ 
>"YW0gY2Fubm90IGJlIHJ1biBpbiBET1MgbW9kZS4NDQokAAAAAAAAAFBFAABMAQMAEM5wWgAAAAAA"+ 
>"AAAA4AAiIAsBMAAAFAAAAAgAAAAAAADeMgAAACAAAABAAAAAAAAQACAAAAACAAAEAAAAAAAAAAQA"+ 
>"AAAAAAAAAIAAAAACAAAAAAAAAwBAhQAAEAAAEAAAAAAQAAAQAAAAAAAAEAAAAAAAAAAAAAAAjDIA"+ 
>"AE8AAAAAQAAAJAQAAAAAAAAAAAAAAAAAAAAAAAAAYAAADAAAAFQxAAAcAAAAAAAAAAAAAAAAAAAA"+ 
>"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAgAAAIAAAAAAAAAAAAAAAIIAAASAAAAAAAAAAA"+ 
>"AAAALnRleHQAAAA8EwAAACAAAAAUAAAAAgAAAAAAAAAAAAAAAAAAIAAAYC5yc3JjAAAAJAQAAABA"+ 
>"AAAABgAAABYAAAAAAAAAAAAAAAAAAEAAAEAucmVsb2MAAAwAAAAAYAAAAAIAAAAcAAAAAAAAAAAA"+ 
>"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"+ 
>"AOkAugIVAHkAxwI9AOkA2wRQABEB5wBWAOkARQUVAOkAUgUGANkA/QRkABkB6wBvACEBhwVzAOkA"+ 
>"YAV6ANkAFgV+AC4ACwC6AC4AEwDDAC4AGwDiAC4AIwDrAC4AKwAAAS4AMwAqAS4AOwAqAS4AQwDr"+ 
>"AC4ASwAwAS4AUwAqAS4AWwAqAS4AYwBVAS4AawB/AUMAWwCMAWMAcwCSAQEAWAAAAAQAXABpAOQy"+ 
>"AAAGAASAAAABAAAAAAAAAAAAAAAAAAcAAAACAAAAAAAAAAAAAACFAN4AAAAAAAIAAAAAAAAAAAAA"+ 
>"AIUA6AQAAAAAAgAAAAAAAAAAAAAAjgDnAgAAAAAAAAAAAQAAAI4EAAAEAAMAAAAASW50MzIAQWlu"+ 
>"Z2VpUmFpNUhhaGZlaVRoZTIAX19TdGF0aWNBcnJheUluaXRUeXBlU2l6ZT04OABBYTZiaTR1aWRh"+ 
>"bjRzaGFoU2VlOQBKb2g4YWNob28xYWVwYWhqZWl5OQA8TW9kdWxlPgA8UHJpdmF0ZUltcGxlbWVu"+ 
>"dGF0aW9uRGV0YWlscz4ANDIwRUVDQjZGQjJBOTREQjJDNjBERjk4QUE5Mjk2MzVENDNCNTk0QgBK"+ 
>"b2plaTVhaHlhaDJ5YWg1bGFlSwBhaHJhaDBpd29DaG9oczJkYWk0YQBtc2NvcmxpYgBBZGQAZ2V0"+ 
>"X0lzQXR0YWNoZWQAbWFnaWNXb3JkAFRhaThBaXAwdWEzVUxpNnpvMWplAFJ1bnRpbWVGaWVsZEhh"+ 
>"bmRsZQBzZXRfTmFtZQBWYWx1ZVR5cGUAQnV0dG9uQmFzZQBDb21waWxlckdlbmVyYXRlZEF0dHJp"+ 
>"YnV0ZQBHdWlkQXR0cmlidXRlAERlYnVnZ2FibGVBdHRyaWJ1dGUAQ29tVmlzaWJsZUF0dHJpYnV0"+ 
>"ZQBBc3NlbWJseVRpdGxlQXR0cmlidXRlAEFzc2VtYmx5VHJhZGVtYXJrQXR0cmlidXRlAEFzc2Vt"+ 
>"Ymx5RmlsZVZlcnNpb25BdHRyaWJ1dGUAQXNzZW1ibHlDb25maWd1cmF0aW9uQXR0cmlidXRlAEFz"+ 
>"c2VtYmx5RGVzY3JpcHRpb25BdHRyaWJ1dGUAQ29tcGlsYXRpb25SZWxheGF0aW9uc0F0dHJpYnV0"+ 
>"ZQBBc3NlbWJseVByb2R1Y3RBdHRyaWJ1dGUAQXNzZW1ibHlDb3B5cmlnaHRBdHRyaWJ1dGUAQXNz"+ 
>"ZW1ibHlDb21wYW55QXR0cmlidXRlAFJ1bnRpbWVDb21wYXRpYmlsaXR5QXR0cmlidXRlAHNldF9T"+ 
>"aXplAHNldF9BdXRvU2l6ZQBzZXRfQ2xpZW50U2l6ZQBTdHJpbmcAU3Vic3RyaW5nAFN5c3RlbS5E"+ 
>"cmF3aW5nAFhhaGh1MmllU2g1aWVGb2hQaUdoAGFpbjdhZWsyVGhhZTNCb2g3b2hoAGF6NW5pZWdo"+ 
>"YWhqMElla2FoMHBoAGdldF9MZW5ndGgATWVlQmlzaDBpb3RobzliaUJ1SmkAYWRkX0NsaWNrAExh"+ 
>"YmVsAEFpbmdlaVJhaTVIYWhmZWlUaGUyLmRsbABDb250cm9sAFN5c3RlbQBGb3JtAEFwcGxpY2F0"+ 
>"aW9uAHNldF9Mb2NhdGlvbgBTeXN0ZW0uUmVmbGVjdGlvbgBDb250cm9sQ29sbGVjdGlvbgBCdXR0"+ 
>"b24AUnVuAGFoSDVlZWRlaVlvaHF1ZWk4Z29vAEVleTRqaWUwcmFlcjdNaWlwaHVvAERlYnVnZ2Vy"+ 
>"AEV2ZW50SGFuZGxlcgBzZXRfVXNlVmlzdWFsU3R5bGVCYWNrQ29sb3IALmN0b3IAU3lzdGVtLkRp"+ 
>"YWdub3N0aWNzAFN5c3RlbS5SdW50aW1lLkludGVyb3BTZXJ2aWNlcwBTeXN0ZW0uUnVudGltZS5D"+ 
>"b21waWxlclNlcnZpY2VzAEFpbmdlaVJhaTVIYWhmZWlUaGUyLnJlc291cmNlcwBEZWJ1Z2dpbmdN"+ 
>"b2RlcwBFdmVudEFyZ3MAQW9mMHJvbzJlZWozYWhTaDFlaXMAZ2V0X0NvbnRyb2xzAFN5c3RlbS5X"+ 
>"aW5kb3dzLkZvcm1zAGdldF9DaGFycwBSdW50aW1lSGVscGVycwBDb25jYXQAT2JqZWN0AERpYWxv"+ 
>"Z1Jlc3VsdABQb2ludABTdXNwZW5kTGF5b3V0AFJlc3VtZUxheW91dABQZXJmb3JtTGF5b3V0AGdl"+ 
>"dF9UZXh0AHNldF9UZXh0AHRhNHZvMkFoazV5YWVwMm9TaHV1AFNob3cAc2V0X1RhYkluZGV4AE1l"+ 
>"c3NhZ2VCb3gAVGV4dEJveABJbml0aWFsaXplQXJyYXkAAAtiAHQAbgBPAGsAABFWAGEAbABpAGQA"+ 
>"YQB0AGUAABFtAGEAZwBpAGMAVAB4AHQAABFtAGEAZwBpAGMATABiAGwAACtFAG4AdABlAHIAIAB0"+ 
>"AGgAZQAgAG0AYQBnAGkAYwAgAHcAbwByAGQAOgAAFVMAbwBTAHQAZQBhAGwAdABoAHkAAEVEAG8A"+ 
>"bgAnAHQAIAB0AHIAeQAgAHkAbwB1AHIAIABkAGkAcgB0AHkAIAB0AHIAaQBjAGsAcwAgAG8AbgAg"+ 
>"AG0AZQAhAAEzWQBvAHUAIABtAHUAcwB0ACAAZgBpAGwAbAAgAHQAaABpAHMAIABmAGkAZQBsAGQA"+ 
>"IQAAK1MAVQBDAEMARQBTAFMAIAAhAAoAUwB1AGIAbQBpAHQAIABOAEQASAB7AAAdfQAgAHQAbwAg"+ 
>"AHYAYQBsAGkAZABhAHQAZQAuAABDWQBPAFUAIABEAEkARABOACcAVAAgAFMAQQBZACAAVABIAEUA"+ 
>"IABNAEEARwBJAEMAIABXAE8AUgBEACAAIQAhACEAAQAAAN90YDtPcQVBvBRJgB7opSwABCABAQgD"+ 
>"IAABBSABARERBCABAQ4EIAEBAgcAAgESZRFpAyAACAQgAQ4IBQABARI9BSACAQgIBSABARF5BSAB"+ 
>"ARF9BSACARwYBiABARKAhQUgABKAiQUgAQESdQcHBQgJAgICBCABAwgFBwMCAgIDAAACBgABEYCV"+ 
>"DgMgAA4GAAMODg4OCLd6XFYZNOCJCLA/X38R1Qo6AwYSQQMGEkUDBhJJAgYOAwYdCAMGERAEIAEC"+ 
>"DgYgAgEcElEIAQAIAAAAAAAeAQABAFQCFldyYXBOb25FeGNlcHRpb25UaHJvd3MBCAEABwEAAAAA"+ 
>"FAEAD0V4YW1wbGVBc3NlbWJseQAAKQEAJEV4YW1wbGUgQXNzZW1ibHkgZm9yIERvdE5ldFRvSlNj"+ 
>"cmlwdAAABQEAAAAAJAEAH0NvcHlyaWdodCDCqSBKYW1lcyBGb3JzaGF3IDIwMTcAACkBACQ1NjU5"+ 
>"OGYxYy02ZDg4LTQ5OTQtYTM5Mi1hZjMzN2FiZTU3NzcAAAwBAAcxLjAuMC4wAAAFAQABAAAEAQAA"+ 
>"AAC0AAAAzsrvvgEAAACRAAAAbFN5c3RlbS5SZXNvdXJjZXMuUmVzb3VyY2VSZWFkZXIsIG1zY29y"+ 
>"bGliLCBWZXJzaW9uPTIuMC4wLjAsIEN1bHR1cmU9bmV1dHJhbCwgUHVibGljS2V5VG9rZW49Yjc3"+ 
>"YTVjNTYxOTM0ZTA4OSNTeXN0ZW0uUmVzb3VyY2VzLlJ1bnRpbWVSZXNvdXJjZVNldAIAAAAAAAAA"+ 
>"AAAAAFBBRFBBRFC0AAAAAAAAABDOcFoAAAAAAgAAABwBAABwMQAAcBMAAFJTRFNLakRIDHHSTac9"+ 
>"HkvNlktIAQAAAEM6XFVzZXJzXGxhYlxEb3dubG9hZHNcRG90TmV0VG9KU2NyaXB0LW1hc3RlclxF"+ 
>"eGFtcGxlQXNzZW1ibHlcb2JqXERlYnVnXEFpbmdlaVJhaTVIYWhmZWlUaGUyLnBkYgAAAAAAAAAA"+ 
>"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"+ 
>"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"+ 
>"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAtDIAAAAAAAAAAAAAzjIA"+ 
>"AAAgAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMAyAAAAAAAAAAAAAAAAX0NvckRsbE1haW4AbXNjb3Jl"+ 
>"ZS5kbGwAAAAAAP8lACAAEBUAAABbAAAAFAAAAAAAAAB+AAAAAAAAAD0AAAAYAAAAAgAAAFIAAAAH"+ 
>"AAAAEQAAAFgAAAAWAAAAEgAAABUAAAByAAAAdQAAAA8AAABQAAAAOwAAABgAAAAAAAAAAAAAAAAA"+ 
>"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"+ 
>"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"+ 
>"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"+ 
>"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABABAAAAAYAACAAAAAAAAAAAAAAAAAAAABAAEA"+ 
>"AAAwAACAAAAAAAAAAAAAAAAAAAABAAAAAABIAAAAWEAAAMgDAAAAAAAAAAAAAMgDNAAAAFYAUwBf"+ 
>"AFYARQBSAFMASQBPAE4AXwBJAE4ARgBPAAAAAAC9BO/+AAABAAAAAQAAAAAAAAABAAAAAAA/AAAA"+ 
>"AAAAAAQAAAACAAAAAAAAAAAAAAAAAAAARAAAAAEAVgBhAHIARgBpAGwAZQBJAG4AZgBvAAAAAAAk"+ 
>"AAQAAABUAHIAYQBuAHMAbABhAHQAaQBvAG4AAAAAAAAAsAQoAwAAAQBTAHQAcgBpAG4AZwBGAGkA"+ 
>"bABlAEkAbgBmAG8AAAAEAwAAAQAwADAAMAAwADAANABiADAAAABiACUAAQBDAG8AbQBtAGUAbgB0"+ 
>"AHMAAABFAHgAYQBtAHAAbABlACAAQQBzAHMAZQBtAGIAbAB5ACAAZgBvAHIAIABEAG8AdABOAGUA"+ 
>"dABUAG8ASgBTAGMAcgBpAHAAdAAAAAAAIgABAAEAQwBvAG0AcABhAG4AeQBOAGEAbQBlAAAAAAAA"+ 
>"AAAASAAQAAEARgBpAGwAZQBEAGUAcwBjAHIAaQBwAHQAaQBvAG4AAAAAAEUAeABhAG0AcABsAGUA"+ 
>"QQBzAHMAZQBtAGIAbAB5AAAAMAAIAAEARgBpAGwAZQBWAGUAcgBzAGkAbwBuAAAAAAAxAC4AMAAu"+ 
>"ADAALgAwAAAAUgAZAAEASQBuAHQAZQByAG4AYQBsAE4AYQBtAGUAAABBAGkAbgBnAGUAaQBSAGEA"+ 
>"aQA1AEgAYQBoAGYAZQBpAFQAaABlADIALgBkAGwAbAAAAAAAYgAfAAEATABlAGcAYQBsAEMAbwBw"+ 
>"AHkAcgBpAGcAaAB0AAAAQwBvAHAAeQByAGkAZwBoAHQAIACpACAASgBhAG0AZQBzACAARgBvAHIA"+ 
>"cwBoAGEAdwAgADIAMAAxADcAAAAAACoAAQABAEwAZQBnAGEAbABUAHIAYQBkAGUAbQBhAHIAawBz"+ 
>"AAAAAAAAAAAAWgAZAAEATwByAGkAZwBpAG4AYQBsAEYAaQBsAGUAbgBhAG0AZQAAAEEAaQBuAGcA"+ 
>"ZQBpAFIAYQBpADUASABhAGgAZgBlAGkAVABoAGUAMgAuAGQAbABsAAAAAABAABAAAQBQAHIAbwBk"+ 
>"AHUAYwB0AE4AYQBtAGUAAAAAAEUAeABhAG0AcABsAGUAQQBzAHMAZQBtAGIAbAB5AAAANAAIAAEA"+ 
>"UAByAG8AZAB1AGMAdABWAGUAcgBzAGkAbwBuAAAAMQAuADAALgAwAC4AMAAAADgACAABAEEAcwBz"+ 
>"AGUAbQBiAGwAeQAgAFYAZQByAHMAaQBvAG4AAAAxAC4AMAAuADAALgAwAAAAAAAAAAAAAAAAAAAA"+ 
>"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"+ 
>"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"+ 
>"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"+ 
>"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"+ 
>"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"+ 
>"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"+ 
>"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"+ 
>"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"+ 
>"AAAAAAAAAAAAAAAAADAAAAwAAADgMgAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"+ 
>"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"+ 
>"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"+ 
>"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"+ 
>"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"+ 
>"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"+ 
>"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"+ 
>"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"+ 
>"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"+ 
>"AAAAAAAAAAAAAAABDQAAAAQAAAAJFwAAAAkGAAAACRYAAAAGGgAAACdTeXN0ZW0uUmVmbGVjdGlv"+ 
>"bi5Bc3NlbWJseSBMb2FkKEJ5dGVbXSkIAAAACgsA"; 
>
>var aiQu9oof = 'AingeiRai5HahfeiThe2';  
>try { 
>	var pohceiC7 = Trololo(dei0eiFu); 
>	var giigh0Ku = new ActiveXObject('System.Runtime.Serialization.Formatters.Binary.BinaryFormatter'); 
>	var eeB9Eisa = new ActiveXObject('System.Collections.ArrayList'); 
> 	var Aik6iulo = giigh0Ku.Deserialize_2(pohceiC7); 
> 	eeB9Eisa.Add(undefined); 
> 	var Aegh5xei = Aik6iulo.DynamicInvoke(eeB9Eisa.ToArray()).CreateInstance(aiQu9oof); 
>  
> 	Aegh5xei.Aa6bi4uidan4shahSee9(dei0eiFu); 
> 	Aegh5xei.Joh8achoo1aepahjeiy9(); 
>} catch (e) { 
>    debug(e.message); 
>}
> ```

We decoded it and put it file, recognised as TrueType font data by the command `strings`. However, `binwalk` gave us a more interesting output:

> ```bash
>binwalk exec 
>
>DECIMAL       HEXADECIMAL     DESCRIPTION
>--------------------------------------------------------------------------------
>1223          0x4C7           Microsoft executable, portable (PE)
>4310          0x10D6          Copyright string: "CopyrightAttribute"
> ```

Using `monodis`, we got an overview of the code ([dump.txt](dump.txt)) and the pseudo-code is as follows (or [here](pseudocode.cs):

> ```cs
>class AingeiRai5HahfeiThe2 extends Form{
>   private Button Aof0roo2eej3ahSh1eis
>    private TextBox ta4vo2Ahk5yaep2oShuu
>    private Label Xahhu2ieSh5ieFohPiGh
>    private string Tai8Aip0ua3ULi6zo1je
>    private int32[] az5nieghahj0Iekah0ph
>
>    public AingeiRai5HahfeiThe2()   {
>		az5nieghahj0Iekah0ph = [0x15,0x5B,0x14,0x00,0x7E,0x00,0x3D,0x18,0x02,0x52,0x07,0x11,0x58,0x16,0x12,0x15,0x72,0x75,0x0F,0x50,0x3B,0x18]
>    }
>
>    public void Aa6bi4uidan4shahSee9 (string ain7aek2Thae3Boh7ohh){
>		Tai8Aip0ua3ULi6zo1je = ain7aek2Thae3Boh7ohh.Substring(ain7aek2Thae3Boh7ohh.get_Length() - 0x16)
>    }
>	
>	public void Joh8achoo1aepahjeiy9 () {
>		Application.Run()
>    }
>
>	//don't really care, it only builds the GUI
>    private void Jojei5ahyah2yah5laeK (){...}
>
>	private bool MeeBish0iotho9biBuJi (string magicWord){
>		for (i = 0; i < Tai8Aip0ua3ULi6zo1je.get_Length(); i++){
>			if (ord(az5nieghahj0Iekah0ph[i]) != ord(Tai8Aip0ua3ULi6zo1je[i])^ord(magicWord[i])){
>				return false
>			}
>			return true
>    }
>
>	private void Eey4jie0raer7Miiphuo (object ahrah0iwoChohs2dai4a, EventArgs ahH5eedeiYohquei8goo)  {
>		if (System.Diagnostics.Debugger::get_IsAttached()){
>			print "Don't try your dirty tricks on me!"
>		}
>		else if (ta4vo2Ahk5yaep2oShuu.get_Text().get_Length()){
>			print "You must fill this field!"
>		}
>		else if(MeeBish0iotho9biBuJi(ta4vo2Ahk5yaep2oShuu.get_Text())){
>			print "SUCCESS !\nSubmit NDH{" + ta4vo2Ahk5yaep2oShuu.get_Text() + "} to validate."
>		}
>		else{
>			print "YOU DIDN'T SAY THE MAGIC WORD !!!"
>		}
>  }
> ```

The cipher uses a simple XOR, where the key is computed in `Aa6bi4uidan4shahSee9`, which is not called in the script. However, in the JS code, we can see
that the argument passed to this routine is the long base64-encoded string:

> ```python
>print ''.join(chr(ord(a)^b) for a,b in zip("FkKEJ5dGVbXSkIAAAACgsA", [0x15,0x5B,0x14,0x00,0x7E,0x00,0x3D,0x18,0x02,0x52,0x07,0x11,0x58,0x16,0x12,0x15,0x72,0x75,0x0F,0x50,0x3B,0x18]))
> ```

And we finally got: **S0_E45Y_T0_B3_ST34L7HY**