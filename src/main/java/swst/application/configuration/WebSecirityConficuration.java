package swst.application.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import lombok.RequiredArgsConstructor;
import swst.application.authenSecurity.filter.CustomAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
//@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class WebSecirityConficuration extends WebSecurityConfigurerAdapter {

	private final UserDetailsService userDetailsService;
	private final BCryptPasswordEncoder passwordEndocer;

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsService).passwordEncoder(passwordEndocer);
	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers(HttpMethod.POST, "/public/auth/**");
		web.ignoring().antMatchers(HttpMethod.GET, "/public/**");
	}

	@Override
	public void configure(HttpSecurity http) throws Exception {
		
		CustomAuthenticationFilter customAuthenticationFilter = new CustomAuthenticationFilter(authenticationManager());
		customAuthenticationFilter.setFilterProcessesUrl("/public/authen");
		http.cors();
		http.csrf().disable();
		http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
		http.authorizeRequests().antMatchers("/public/**").permitAll();
		http.authorizeRequests().antMatchers(HttpMethod.GET,"/admin/ListUsers").hasAnyAuthority("customer");
		http.authorizeRequests().anyRequest().authenticated();

		http.addFilter(customAuthenticationFilter);
	}

	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}

	/*
	 * .and().csrf().disable().addFilterBefore(corsFilter,
	 * UsernamePasswordAuthenticationFilter.class)
	 * .exceptionHandling().authenticationEntryPoint(authenticationErrorHandler)
	 * .accessDeniedHandler(jwtAccessDeniedHandler).and().headers().frameOptions().
	 * sameOrigin()
	 * 
	 * .and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.
	 * STATELESS)
	 * 
	 * .and().authorizeRequests().antMatchers("/public/**").permitAll()
	 * 
	 * .anyRequest().authenticated().and().apply(securityConfigurerAdapter());
	 */
	/*
	 * private JWTConfigurer securityConfigurerAdapter() { return new
	 * JWTConfigurer(tokenPovider); }
	 */

}
