/**
* @file re_hmac.h  Interface to HMAC functions
*
* Copyright (C) 2010 Creytiv.com
*/

#ifndef HMAC_H_
#define HMAC_H_ (1)

#include <stdint.h>

void hmac_sha1(const char *k,   /* secret key */
               size_t lk,  /* length of the key in bytes */
               const char *d,   /* data */
               size_t ld,  /* length of data in bytes */
               char *out, /* output buffer, at least "t" bytes */
               size_t *t);

#endif // HMAC_H_
