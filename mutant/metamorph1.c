#include <stdio.h>
#include <stdlib.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include <string.h>
#include <time.h>
#include <elf.h>
#include <sys/mman.h>

#define SHELLCODE_SIZE 102
static unsigned char shellcode[SHELLCODE_SIZE] = "\xeb\x00R\xeb\x00W\xeb\x00V\xeb\x00h-l\x00\x00\xeb\x00h/ls\x00\xeb\x00H\x83\xec\x04\xeb\x00\xc7\x04$/bin\xeb\x00j\x00\xeb\x00H\x8d|$\x14\xeb\x00W\xeb\x00H\x8d|$\x10\xeb\x00W\xeb\x00H\x89\xe6\xeb\x00\xba\x00\x00\x00\x00\xeb\x00\xb8;\x00\x00\x00\xeb\x00H\x83\xc4,\xeb\x00\x0f\x05\xeb\x00^\xeb\x00_\xeb\x00Z\xeb\x00";

typedef struct Instr_struct{
	char* i_code;		//shellcode "chunk"
	char  i_code_len;	//length of the chunk
	char  i_index;		//index in the shellcode
	char  i_jump;		//jump to reach next instruction
} Instr;

void die(char*,char*);
Elf64_Shdr* get_section(void*);
void mutate(char*);
int set_jumps(Instr**, int*, int);
Instr** split_instructions(char*, int);

int main(int argc, char** argv){
	char* data; 
	struct stat info; 
	int fd;
	
	/** Reads itself **/
	if ((fd = open(argv[0], O_RDONLY, 0)) < 0) die(NULL, "Could not read myself\n"); 
	fstat(fd, &info); 
	if (!(data = malloc(info.st_size))) die(data, "Could not allocate memory\n"); 
	read(fd, data, info.st_size); 
	close(fd); 
	
	mutate(data);
	
	/** Overwrites itself **/
	if (unlink(argv[0]) < 0) die(data, "Could not unlink myself\n"); 
	if ((fd = open(argv[0], O_CREAT|O_TRUNC|O_RDWR, S_IRWXU)) < 0) die(data, "Could not re-create myself\n"); 
	if (write(fd, data, info.st_size) < 0) die(data, "Could not re-write myself\n"); 
	close(fd);
	free(data);
	/** Execute shellcode **/
	uintptr_t pagestart	 = (uintptr_t)shellcode & -getpagesize();
	if (mprotect((void*)pagestart, ((uintptr_t)shellcode + SHELLCODE_SIZE - pagestart), PROT_READ | PROT_WRITE | PROT_EXEC) < 0)
		die(data, "Could not change permissions before executing. Exiting\n");
	((void(*)())shellcode)();
}	

/**
 * Exits by printing an error message.
 * Debugging purpose
 * Frees file's content
 * **/
void die(char* p_data, char* p_msg){
	if (p_data) free(p_data); 
	fprintf(stderr, p_msg, NULL); 
	exit(EXIT_FAILURE); 
}

/**
 * Reads file content and finds .data section.
 * Returns a pointer on Elf64_Shdr containing .data's header infos
 * **/
Elf64_Shdr* get_section(void* p_data){
	int i;
	Elf64_Ehdr* elf_header = (Elf64_Ehdr*) p_data;
	Elf64_Shdr* section_header_table = (Elf64_Shdr*) (p_data + elf_header->e_shoff);
	char* strtab_ptr = p_data + section_header_table[elf_header->e_shstrndx].sh_offset;
	for (i = 0; i < elf_header->e_shnum; i++){
		if (!strcmp(strtab_ptr + section_header_table[i].sh_name, ".data")) return &section_header_table[i];
	}
	return NULL;
}

/**
 * Reorganizes the shellcode instructions and computes new jumps
 * Modifies the string p_data with the new shellcode, in the .data section
 * **/
void mutate(char* p_data){
	int nb_instr = 0, i, j;
	unsigned char count;
	char* cursor = malloc(SHELLCODE_SIZE);
	Instr* instr = NULL;
	
	/** Count how many jumps there are **/
	for (i = 0; i < SHELLCODE_SIZE; i++){
		if (shellcode[i] == 0xeb) nb_instr++;
	}
	nb_instr--; //because there are n "useful" instructions and n+1 jumps
	
	//instrs: array containing all "useful" instructions, in the right order 
	Instr** instrs = split_instructions(p_data, nb_instr);
		
	/** Shuffle **/
	//instrs_idx: contains the indexes of the instructions, in the shuffled array
	//at the beginning, instrs_idx[i] = i since there are in the right order
	int instrs_idx[nb_instr];
	for (i = 0; i < nb_instr; i++)
		instrs_idx[i] = i;

	srand(time(NULL));
	do{
		for (i = 0; i < nb_instr -1; i++) {
			j = i + rand() / (RAND_MAX / (nb_instr - i) + 1);
			//swap instructions
			instr = instrs[i];
			instrs[i] = instrs[j];
			instrs[j] = instr;
			//swap the indexes, but according to the i_index!
			int idx = instrs_idx[instrs[i]->i_index];
			instrs_idx[instrs[i]->i_index] = instrs_idx[instrs[j]->i_index];
			instrs_idx[instrs[j]->i_index] = idx;
		}
	}
	while((count = set_jumps(instrs, instrs_idx, nb_instr)) == 0xff); //if all offsets are okay, continue
	//~ for (i = 0; i < nb_instr; i++){
		//~ printf("Instr%d\n", i);
		//~ printf("	Code length: %d\n", instrs[i]->i_code_len);
		//~ printf("	Index      : %d\n", instrs[i]->i_index);
		//~ printf("	Jump       : %d\n", instrs[i]->i_jump);
		//~ printf("	Code       : ");
		//~ for (j = 0; j < instrs[i]->i_code_len; j++){
			//~ printf("%x ", *(instrs[i]->i_code+j));
		//~ }
		//~ printf("\n");
		
	//~ }
	
	/** Build the new shellcode **/
	*cursor++ = 0xeb;
	*cursor++ = count; //initial jump distance
	for (i = 0; i < nb_instr; i++){
		instr = instrs[i];
		for (j = 0; j < instr->i_code_len; j++){
			*cursor = instr->i_code[j]; cursor++; // append "useful" instruction's code
		}
		*cursor++ = 0xeb; //jmp opcode
		*cursor++ = instr->i_jump; //distance to reach next instruction
	}
	
	/** Replace the shellcode in p_data**/
	cursor -= SHELLCODE_SIZE; //replace the cursor at the beginning
	
	Elf64_Shdr *sec_hdr;
	if ((sec_hdr = get_section(p_data)))
		memcpy(p_data+sec_hdr->sh_offset + 0x20, cursor, SHELLCODE_SIZE); //+32 is the offset of the shellcode in .data
	/** Destroy instructions **/
	for (i = 0; i < nb_instr; i++){
		free(instrs[i]);
	}
	free(instrs);
	free(cursor);
}

/**
 * For each instruction, computes the offset to reach the
 * next instruction
 * Returns the first jump distance if none distance is equal to 0xeb, or -1
 * **/
int set_jumps(Instr** p_instrs, int* p_instrs_idx, int p_nb_instrs){
	int i, j, idx, idx1;
	unsigned char count;
	
	/** Set jumps BETWEEN "useful" instructions**/
	for (i = 0; i < p_nb_instrs -1; i++){
		count = 0;
		idx = p_instrs_idx[i];		//index of the current instruction in shuffled array
		idx1 = p_instrs_idx[i+1];	//index next instruction (to execute) in shuffled array 
		if (idx1 > idx){
			//move forward
			//start from the NEXT instruction IN THE ARRAY before reaching the next instruction TO EXECUTE
			for (j = idx+1; j < idx1; j++)
				count += (2 + p_instrs[j]->i_code_len); //+2 because jump instruction is 2-bytes long
		}
		else{
			//move backward
			//start from CURRENT instruction until reaching the next instruction TO EXECUTE
			for (j = idx; j >= idx1; j--)
				count -= (2 + p_instrs[j]->i_code_len); //same reason for -2
		}
		p_instrs[idx]->i_jump = count;
		if (count == 0xeb) return -1; //if the jump is equal to 0xeb (also jmp's opcode), return -1
	}
	
	/** Set initial jump (reach the first "useful" instruction) **/ 
	count = 0;
	for (i = 0; i < p_nb_instrs && i < p_instrs_idx[0]; i++) //instrs_idx[0] contains the index in the shuffled array of the first instruction to execute
		count += 2 + p_instrs[i]->i_code_len;
	return (count == 0xeb) ? -1 : count;
}

/**
 * Reads the shellcode (not necessarily the same as the one written above since the binary has been overwritten) and
 * splits instructions
 * Pushes them in an array in the order they will be executed
 * **/
Instr** split_instructions(char* p_data, int p_nb_instrs){
	Instr** instrs = malloc(p_nb_instrs * sizeof(Instr*));	
	int i, j;
	unsigned char k;
	for (i = 0; i < p_nb_instrs; i++){
		instrs[i] = malloc(sizeof(Instr));
	}
	j = shellcode[1] + 2; //first opcode after initial jump: shellcode[1] is the distance and +2 is for the jmp instruction itself
	for(i = 0; i < p_nb_instrs; i++){
		k = j + 1; //second char of next instruction
		while (k < SHELLCODE_SIZE && shellcode[k] != 0xeb) k++;
		//now, k = next jmp
		/** Next jump has been found. Shellcode[j+1:k] is an instruction **/
		instrs[i]->i_code_len = k - j;
		instrs[i]->i_code = &shellcode[j];
		instrs[i]->i_index = i;
		instrs[i]->i_jump = (char)shellcode[k + 1];
		j = k + instrs[i]->i_jump + 2 ; //jumps to next instruction TO EXECUTE 
	}
	return instrs;
}
