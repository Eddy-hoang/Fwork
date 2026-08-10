package com.intern.fwork;

import io.jsonwebtoken.Jwts;

public class GenerateSecret {
    public static void main(String[] args) {

        System.out.println(
                io.jsonwebtoken.io.Encoders.BASE64.encode(
                        Jwts.SIG.HS512.key().build().getEncoded()
                )
        );

    }
}
