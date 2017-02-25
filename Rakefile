require "rubygems"
require "tmpdir"

require "bundler/setup"
require "jekyll"

GITHUB_REPONAME = "devth/devth.github.com"

desc "Generate blog files"
task :generate do
  Jekyll::Site.new(Jekyll.configuration({
    "source"      => ".",
    "destination" => "_site"
  })).process
end


desc "Generate and publish static blog to master"
task :publish => [:generate] do
  Dir.mktmpdir do |tmp|
    cp_r "_site/.", tmp

    pwd = Dir.pwd
    Dir.chdir tmp

    system "git init"
    system "git add ."

    system 'git config user.name "Trevor Hartman"'
    system "git config user.email 'trevorhartman@gmail.com'"
    gh_token = ENV['GH_TOKEN']
    system "git remote add origin https://#{gh_token}@github.com/#{GITHUB_REPONAME}.git"
    message = "Generate updated site at #{Time.now.utc}"
    system "git commit -m #{message.inspect}"
    system "git push origin master --force"

    Dir.chdir pwd
  end
end
