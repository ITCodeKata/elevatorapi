#!/usr/bin/env bash
consul-template -consul-addr=bk-consul-1001:8500 -template "src/main/resources/consul-client-privilege.ctmpl:target/scala-2.12/classes/consul-client-privilege.conf"
