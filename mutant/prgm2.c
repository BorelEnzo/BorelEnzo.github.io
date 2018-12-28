#include <time.h>
#include <sys/stat.h>
#include <sys/mman.h>
#include <fcntl.h>
#include <unistd.h>
#include <elf.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define KEY_SIZE 8
#define SECTION "..text"
#define CRYPT  __attribute__((section(SECTION)))
static unsigned char key[KEY_SIZE + 1] = "\1\1\1\1\1\1\1\1";
extern char __executable_start;

void die(char*,char*);
Elf64_Shdr* get_section(void*, char*);
void mutate(char*, char*, int);
void xor(unsigned char*, int);
void xyz();

int main(int argc, char** argv){
	srand(time(NULL));
	int fd;
	char* my_data;
	
	if ((fd = open(argv[0], O_RDONLY, 0)) < 0) die(NULL, "Could not read file. Exiting\n");
	struct stat info;
	fstat(fd, &info);
	if (!(my_data = malloc(info.st_size))) die(NULL, "Could not allocate memory. Exiting\n");
	read(fd, my_data, info.st_size); 
	close(fd);
	
	mutate(my_data, argv[0], info.st_size);
	xyz();
	free(my_data);
	return 0;
}

void die(char* p_data, char* p_msg){
	if (p_data) free(p_data);
	fprintf(stderr, p_msg, NULL);
	exit(EXIT_FAILURE);
}

Elf64_Shdr* get_section(void* p_data, char* p_section){
	int i;
	Elf64_Ehdr* elf_header = (Elf64_Ehdr*) p_data;
	Elf64_Shdr* section_header_table = (Elf64_Shdr*) (p_data + elf_header->e_shoff);
	char* strtab_ptr = p_data + section_header_table[elf_header->e_shstrndx].sh_offset;
	for (i = 0; i < elf_header->e_shnum; i++){
		if (!strcmp(strtab_ptr + section_header_table[i].sh_name, p_section)) return &section_header_table[i];
	}
	return NULL;
}

void mutate(char* p_data, char* p_fname, int p_fsize){
	Elf64_Shdr *sec_hdr;
	int key_off, i;
	
	if (!(sec_hdr = get_section(p_data, ".data"))) die(p_data, "Could not find .data section. Exiting\n");
	key_off = sec_hdr->sh_offset + 16; // __data_start + __dso_handle -> +16 bytes
	
	if (!(sec_hdr = get_section(p_data, SECTION))) die(p_data, "Could not find secured section. Exiting\n");
		
	unsigned char *start = &__executable_start + sec_hdr->sh_offset;
	uintptr_t pagestart	 = (uintptr_t)start & -getpagesize();
	int psize			 = start + sec_hdr->sh_size - (unsigned char*)pagestart;	
	if (mprotect((void*)pagestart, psize, PROT_READ | PROT_WRITE | PROT_EXEC) < 0) die(p_data, "Could not make page writable memory. Exiting\n");
	
	unsigned char* ptr_seg = p_data + sec_hdr->sh_offset;
	xor(start, sec_hdr->sh_size);
	xor(ptr_seg, sec_hdr->sh_size);
	
	if (mprotect((void*)pagestart, psize, PROT_READ | PROT_EXEC) < 0) die(p_data, "Could not reset permissions. Exiting\n");
	
	unsigned char* key_addr = p_data + key_off;
	for (i = 0; i < KEY_SIZE; i++)
		key_addr[i] = key[i] = (rand() % 255);
	
	xor(ptr_seg, sec_hdr->sh_size);
	if (unlink(p_fname) < 0) die(p_data, "Could not unlink file. Exiting\n");
	int fd = open(p_fname, O_CREAT | O_TRUNC | O_RDWR, S_IRWXU);
	if (fd < 0) die(p_data, "Could not recreate file after unlinking. Exiting\n");
	if (write(fd, p_data, p_fsize) < 0) die(p_data, "Could not write file. Exiting\n");
	close (fd);
}

void xor(unsigned char *p_data, int p_len){
	int i;
	for(i = 0; i < p_len; i++)
		p_data[i] ^= (key[i % KEY_SIZE] - 1);
}

CRYPT
void xyz(){
	printf("Hello world!\n");
	return;
}
