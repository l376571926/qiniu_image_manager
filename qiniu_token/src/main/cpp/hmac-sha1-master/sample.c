/*
 * @file sample.c
 * @author Akagi201
 * @date 2014/12/05
 */

#include <stdio.h>
#include <string.h>
#include <hmac/hmac.h>

int main(void) {
    char sec_key[] = "Rqtlx1-hJZj6QpRoldtMfy7kdG6_aj4T36QA6GDB";
    char data[] = "http://rvruzr2c3.hn-bkt.clouddn.com/my-java.png?e=1686040163";
    char out[256] = {0};
    int len = sizeof(out);

    hmac_sha1(sec_key, strlen(sec_key), data, strlen(data), out, &len);

    printf("len: %d\n", len);

    return 0;
}