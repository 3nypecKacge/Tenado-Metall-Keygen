#include "nolibc/crt.h"
#include "nolibc/arch.h"
#include "nolibc/stdio.h"

// errno

static int g_errno = 0;

int *__errno(void) {
    return &g_errno;
}