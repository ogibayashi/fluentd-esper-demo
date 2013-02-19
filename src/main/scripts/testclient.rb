#!/usr/bin/env ruby
require 'msgpack/rpc'
require 'optparse'

OPTS = {}
ARGV.options {|opt|
  opt.on('-l', 'List events.'){|v| OPTS[:list] =  v }
  opt.on('-s', 'List statements.'){|v| OPTS[:statement] =  v }
  opt.on('-d VAL', 'List statements.'){|v| OPTS[:delete] =  v }
  opt.on('-n VAL', 'Statment name' ){ |v| OPTS[:name] = v}
  opt.parse!
}


client = MessagePack::RPC::Client.new('127.0.0.1',1985)
if OPTS[:list]
  puts client.call(:listschema)
elsif OPTS[:statement]
  puts client.call(:liststatements)
elsif OPTS[:delete]
  puts OPTS[:delete]
  client.call(:removestatement, OPTS[:delete])
end
ARGV.each{ |f|
  File.open(f).readlines.each{|q|
    puts client.call(:create, q, OPTS[:name] || f)
  }
}
