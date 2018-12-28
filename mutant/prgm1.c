#include <stdio.h>
#include <stdlib.h>
#include <sys/mman.h>
#include <unistd.h>
#include <string.h>

void foo();
int main(int argc, char ** argv){
	void* foo_ptr = (void*) foo;
	foo();
	
    void* start_page = (void*)((long)foo_ptr & -getpagesize());
    if(mprotect(start_page, getpagesize(), PROT_READ | PROT_WRITE | PROT_EXEC) < 0) {
		fprintf(stderr, "Could not mprotect\n");
		exit(EXIT_FAILURE);
    }
    char* shellcode =
    "\x52"							//push   %rdx
    "\x57"							//push   %rdi
	"\x56"							//push   %rsi
	"\x48\x8d\x3d\x1e\x00\x00\x00"	//lea    0x1e(%rip),%rdi        # 4000a8 <msg>
	"\x6a\x00"						//pushq  $0x0
	"\x57"							//push   %rdi
	"\x48\x89\xe6"					//mov    %rsp,%rsi
	"\xba\x00\x00\x00\x00"			//mov    $0x0,%edx
	"\xb8\x3b\x00\x00\x00"			//mov    $0x3b,%eax
	"\x0f\x05"						//syscall
	"\x5e"							//pop    %rsi
	"\x5e"							//pop    %rsi
	"\x5e"							//pop    %rsi
	"\x5f"							//pop    %rdi
	"\x5a"							//pop    %rdx
	"\xc3"							//retq
	"\x90"							//nop
	"\x90"							//nop
	"\x90"							//nop
	"\x90"							//nop
	"\x90"							//nop
	"\x90"							//nop
	"\x2f\x75\x73\x72\x2f\x62\x69\x6e\x2f\x69\x64\x00";	// /usr/bin/id + null byte

    memmove(foo_ptr, shellcode, 52);
    foo();
    return 0;
}

void foo(){
	printf("I'm a foo fighter\n");
}
